package dev.rudrade.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.repository.UserRepository;
import dev.rudrade.chat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = {"/sql-scripts/users.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS
)
class MessageControllerTest extends SqlIntegrationTest {

    private WebSocketStompClient wsClient;
    private List<StompSession> sessions;

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    
    @LocalServerPort private int port;
    private String url;

    @BeforeEach
    void setup() {
        wsClient = new WebSocketStompClient(new StandardWebSocketClient());
        wsClient.setMessageConverter(new JacksonJsonMessageConverter());

        url = "ws://localhost:"+port+"/ws";
        sessions = new ArrayList<>();
    }

    @AfterEach
    void after() {
        sessions.forEach(s -> {
            if (s.isConnected()) {
                s.disconnect();
            }
        });
    }

    //=========
    //  send
    //=========

    // 3 users connected, a sends msg to b, c can't get response
    @Test
    void itShouldSendToUser() throws Exception{
        var user1 = userRepository.findByUsername("user-test").get();
        var user2 = userRepository.findByUsername("user-test-2").get();
        var user3 = userRepository.findByUsername("user-test-3").get();

        var token1 = jwtUtil.generateToken(user1);
        var token2 = jwtUtil.generateToken(user2);
        var token3 = jwtUtil.generateToken(user3);

        var future1 = new CompletableFuture<MessageDto>();
        var future2 = new CompletableFuture<MessageDto>();
        var future3 = new CompletableFuture<MessageDto>();

        var session1 = connect(token1);
        assertTrue(session1.isConnected());
        var session2 = connect(token2);
        assertTrue(session2.isConnected());
        var session3 = connect(token3);
        assertTrue(session3.isConnected());

        session1.subscribe("/user/topic/messages", new MessageHandler(future1));
        session2.subscribe("/user/topic/messages", new MessageHandler(future2));
        session3.subscribe("/user/topic/messages", new MessageHandler(future3));

        var payload = new MessageInputDto(null, user2.getId(), "test 1on1");
        session1.send("/app/sendMessage", payload);

        var result1 = future1.get(5, TimeUnit.SECONDS);
        var result2 = future2.get(5, TimeUnit.SECONDS);

        assertThrows(TimeoutException.class, 
            () -> future3.get(5, TimeUnit.SECONDS)
        );

        var results = List.of(result1, result2);
        assertThat(results)
            .isNotEmpty()
            .allSatisfy(msg -> {
                assertNotNull(msg);
                assertNotNull(msg.id());
                assertEquals(user1.getId(), msg.idFrom());
                assertNotNull(msg.idChatTo());
                assertEquals("test 1on1", msg.text());
                assertThat(msg.dtSent())
                    .isNotNull()
                    .isCloseTo(LocalDateTime.now(), within(1L, ChronoUnit.MINUTES));
            });
    }

    // 4 users connected, 4 (1 not connected) in 1 chat, 1 of them sends the message, all 3 (self+2 connected) get response
    @Sql("/sql-scripts/messages-send.sql")
    @Test
    void itShouldSendToChat() throws Exception {
        var user1 = userRepository.findByUsername("user-test").get();
        var user2 = userRepository.findByUsername("user-test-2").get();
        var user3 = userRepository.findByUsername("user-test-3").get();
        var user4 = userRepository.findByUsername("user-test-5").get();

        var token1 = jwtUtil.generateToken(user1);
        var token2 = jwtUtil.generateToken(user2);
        var token3 = jwtUtil.generateToken(user3);
        var token4 = jwtUtil.generateToken(user4);

        var future1 = new CompletableFuture<MessageDto>();
        var future2 = new CompletableFuture<MessageDto>();
        var future3 = new CompletableFuture<MessageDto>();
        var future4 = new CompletableFuture<MessageDto>();

        var session1 = connect(token1);
        assertTrue(session1.isConnected());
        var session2 = connect(token2);
        assertTrue(session2.isConnected());
        var session3 = connect(token3);
        assertTrue(session3.isConnected());
        var session4 = connect(token4);
        assertTrue(session4.isConnected());

        session1.subscribe("/user/topic/messages", new MessageHandler(future1));
        session2.subscribe("/user/topic/messages", new MessageHandler(future2));
        session3.subscribe("/user/topic/messages", new MessageHandler(future3));
        session4.subscribe("/user/topic/messages", new MessageHandler(future4)); // This cannot receive

        var chatId = UUID.fromString("6fd61b05-4052-4a5d-881d-72ae3f1cbff6");
        var payload = new MessageInputDto(chatId, null, "test text");
        session1.send("/app/sendMessage", payload);

        var result1 = future1.get(5, TimeUnit.SECONDS);
        var result2 = future2.get(5, TimeUnit.SECONDS);
        var result3 = future3.get(5, TimeUnit.SECONDS);
        
        // verify did not receive anything
        assertThrows(TimeoutException.class, 
            () -> future4.get(5, TimeUnit.SECONDS)
        );

        var results = List.of(result1, result2, result3);
        assertThat(results)
            .isNotEmpty()
            .allSatisfy(msg -> {
                assertNotNull(msg);
                assertNotNull(msg.id());
                assertEquals(user1.getId(), msg.idFrom());
                assertEquals(chatId, msg.idChatTo());
                assertEquals("test text", msg.text());
                assertThat(msg.dtSent())
                    .isNotNull()
                    .isCloseTo(LocalDateTime.now(), within(1L, ChronoUnit.MINUTES));
            });
    }

    //==================
    //  findSummaries
    //==================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30", // Invalid
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjaGF0YXBwIiwic3ViIjoiMjlhOGQ5NjAtNDZkMi00ZTU1LTgwYWItN2Y2NDc3NTQxYTczIiwiaWF0IjoxNDIwMDcwNDAwLCJleHAiOjE0MjAwNzA0MDB9.Uu53P1zZ68t5HaqF1rDXRg2_6LoRccrIziTHDQSksa8" // Expired
    })
    void itShouldNotConnectWhenTryingToConnectWithInvalidToken(String token) {
        var handshakeHeader = new WebSocketHttpHeaders();
        if (token != null)
            handshakeHeader.add(HttpHeaders.AUTHORIZATION, "Bearer "+token);

        var connectHeaders = new StompHeaders();
        if (token != null)
            connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer "+token);

        var cause = assertThrows(ExecutionException.class, 
            () -> {
                var session = wsClient.connectAsync(url, handshakeHeader, connectHeaders, new StompSessionHandlerAdapter() {}).get();
                sessions.add(session);
            }
        ).getCause();

        assertThat(cause.getMessage()).containsAnyOf("401", "403", "Connection closed");
    }

    @Sql("/sql-scripts/messages-summary.sql")
    @Test
    void findSummaries() throws Exception {
        var user1 = userRepository.findByUsername("user-test");
        var token1 = jwtUtil.generateToken(user1.get());
        var future1 = new CompletableFuture<List<MessageSummaryDto>>();

        var user2 = userRepository.findByUsername("user-test-2");
        var token2 =  jwtUtil.generateToken(user2.get());
        var future2 = new CompletableFuture<List<MessageSummaryDto>>();

        // Main session
        var session = connect(token1);
        assertTrue(session.isConnected());

        // Second sesssion -> Can't get things
        var session2 = connect(token2);
        assertTrue(session2.isConnected());
        
        session2.subscribe("/user/topic/summaries", new MessageSummaryHandler(future2));
        
        session.subscribe("/user/topic/summaries", new MessageSummaryHandler(future1));
        session.send("/app/search/temp", null);

        var result = future1.get(5, TimeUnit.SECONDS);
        assertThat(result).hasSize(10);

        assertThrows(TimeoutException.class, 
            () -> future2.get(5, TimeUnit.SECONDS)
        );

        var name = new ArrayList<String>();
        var messages = new ArrayList<String>();

        result.forEach(u -> {
            name.add(u.name());
            if (u.lastMessage() != null) {
                messages.add(u.lastMessage());
                assertNotNull(u.dtSent());
                assertNotNull(u.chatId());
            }
        });

        assertThat(messages).hasSize(4).containsExactlyInAnyOrder("msg-2", "msg-3", "msg-5-1", "msg-5-2");

        var usersDb = new ArrayList<String>();
        usersDb.add("temp-5"); // temp-5 appears twice because has 2 active chats w/ the user
        userRepository.findAll().forEach(u -> {
            if (u.isActive() && u.getName().toLowerCase().contains("temp")) {
                usersDb.add(u.getName());
            }
        });

        assertThat(name).hasSize(10).containsExactlyInAnyOrderElementsOf(usersDb);

        assertThat(result)
            .usingDefaultElementComparator()
            .doesNotHaveDuplicates();
    }

    //==========
    //  utils
    //==========

    private StompSession connect(String token) {
        try {
            var session = wsClient.connectAsync(url, new WebSocketHttpHeaders(), header(token), new StompSessionHandlerAdapter(){}).get(5, TimeUnit.SECONDS);
            sessions.add(session);
            return session;
        } catch (Exception ex) {
            fail(ex);
            return null;
        }
    }

    private StompHeaders header(String token) {
        var header = new StompHeaders();
        header.add(HttpHeaders.AUTHORIZATION, "Bearer "+token);
        return header;
    }

    private int requests;
    @RequiredArgsConstructor
    private class MessageSummaryHandler implements StompFrameHandler {

        private final CompletableFuture<List<MessageSummaryDto>> future;

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return List.class;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void handleFrame(StompHeaders headers, @Nullable Object payload) {
            log.debug("Starting parsing");
            requests++;

            log.debug("session:"+headers+"|requests:"+requests);

            if (requests > 1) {
                future.completeExceptionally(new IllegalStateException("Got 2nd request for session:"+headers.getSession()));
                return;
            }

            var result = new ArrayList<MessageSummaryDto>();

            var parsed = (List<Map>) payload;
            parsed.forEach(map  -> {
                // Using this validation for nulls, otherwise ObjectMapper gets stuck
                var strChatId = (String) map.get("chatId");
                var chatId = strChatId != null ? UUID.fromString(strChatId) : null;
                var strUserId = (String) map.get("userId");
                var userId = strUserId != null ? UUID.fromString(strUserId) : null;
                var name = (String) map.get("name");
                var online = (boolean) map.get("online");
                var lastMessage = (String) map.get("lastMessage");
                var strDtSent = (String) map.get("dtSent");
                var dtSent = strDtSent != null ? LocalDateTime.parse(strDtSent) : null;
                var image = (String) map.get("image");

                result.add(new MessageSummaryDto(chatId, userId, name, online, lastMessage, dtSent, image));
            });
            
            log.debug("End parsing, calling complete");
            future.complete(result);
        }
        
    }

    @RequiredArgsConstructor
    private class MessageHandler implements StompFrameHandler {

        private final CompletableFuture<MessageDto> future;

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return MessageDto.class;
        }
        @Override
        public void handleFrame(StompHeaders headers, @Nullable Object payload) {
            log.debug("handling payload: {}", payload);
            future.complete((MessageDto) payload);
        }

    }

}
