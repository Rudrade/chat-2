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
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql({"/sql-scripts/users.sql","/sql-scripts/messages.sql"})
class MessageControllerTest extends SqlIntegrationTest {
    
    private WebSocketStompClient wsClient;
    private CompletableFuture<List<MessageSummaryDto>> completableFuture;

    @Autowired private UserRepository userRepository;
    
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

    @Test
    void findSummaries() throws Exception {
        var connectHeaders = new StompHeaders();
        connectHeaders.add("userId", "29a8d960-46d2-4e55-80ab-7f6477541a28");

        // Main session
        var session = wsClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get();
        assertTrue(session.isConnected());

        // Second sesssion -> Can't get things
        var connectHeaders2 = new StompHeaders();
        connectHeaders2.add("userId", "29a8d960-46d2-4e55-80ab-7f6477541c28");

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
