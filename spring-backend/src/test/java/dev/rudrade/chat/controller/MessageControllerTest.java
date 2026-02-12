package dev.rudrade.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
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
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.repository.UserRepository;
import dev.rudrade.chat.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = {"/sql-scripts/users.sql","/sql-scripts/messages.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class MessageControllerTest extends SqlIntegrationTest {
    
    private WebSocketStompClient wsClient;
    private CompletableFuture<List<MessageSummaryDto>> completableFuture;

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    
    @LocalServerPort private int port;
    private String url;

    @BeforeEach
    void setup() {
        wsClient = new WebSocketStompClient(new StandardWebSocketClient());
        wsClient.setMessageConverter(new JacksonJsonMessageConverter());

        completableFuture = new CompletableFuture<>();
        url = "ws://localhost:"+port+"/ws";
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
            () -> wsClient.connectAsync(url, handshakeHeader, connectHeaders, new StompSessionHandlerAdapter() {}).get()
        ).getCause();

        assertThat(cause.getMessage()).containsAnyOf("401", "403", "Connection closed");
    }

    @Test
    void findSummaries() throws Exception {
        var user1 = userRepository.findByUsername("user-test");
        var token1 = jwtUtil.generateToken(user1.get());

        var user2 = userRepository.findByUsername("user-test-2");
        var token2 =  jwtUtil.generateToken(user2.get());

        var connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer "+token1);

        // Main session
        var session = wsClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get();
        assertTrue(session.isConnected());

        // Second sesssion -> Can't get things
        var connectHeaders2 = new StompHeaders();
        connectHeaders2.add(HttpHeaders.AUTHORIZATION, "Bearer "+token2);

        var session2 = wsClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders2, new StompSessionHandlerAdapter() {}).get();
        assertTrue(session2.isConnected());
        
        session2.subscribe("/user/topic/summaries", new MessageSummaryHandler());
        
        session.subscribe("/user/topic/summaries", new MessageSummaryHandler());
        session.send("/app/search/temp", null);

        var result = completableFuture.get(5, TimeUnit.SECONDS);
        assertThat(result).hasSize(3);

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

        assertThat(messages).hasSize(2).containsExactlyInAnyOrder("msg-2", "msg-3");

        var usersDb = new ArrayList<String>();
        userRepository.findAll().forEach(u -> {
            if (u.isActive() && u.getName().toLowerCase().contains("temp")) {
                usersDb.add(u.getName());
            }
        });

        assertThat(name).hasSize(3).containsExactlyInAnyOrderElementsOf(usersDb);
    }
    
    private int requests;
    private class MessageSummaryHandler implements StompFrameHandler {

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
                completableFuture.completeExceptionally(new IllegalStateException("Got 2nd request for session:"+headers.getSession()));
                return;
            }

            var result = new ArrayList<MessageSummaryDto>();

            var parsed = (List<Map>) payload;
            parsed.forEach(map  -> {
                // Using this validation for nulls, otherwise ObjectMapper gets stuck
                var strChatId = (String) map.get("chatId");
                var chatId = strChatId != null ? UUID.fromString(strChatId) : null;
                var name = (String) map.get("name");
                var online = (boolean) map.get("online");
                var lastMessage = (String) map.get("lastMessage");
                var strDtSent = (String) map.get("dtSent");
                var dtSent = strDtSent != null ? LocalDateTime.parse(strDtSent) : null;
                var image = (String) map.get("image");

                result.add(new MessageSummaryDto(chatId, name, online, lastMessage, dtSent, image));
            });
            
            log.debug("End parsing, calling complete");
            completableFuture.complete(result);
        }
        
    }

}
