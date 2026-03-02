package dev.rudrade.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import dev.rudrade.chat.ControllerIntegrationTest;

@Sql({"/sql-scripts/users.sql","/sql-scripts/messages-latest.sql"})
class RestMessageControllerTest extends ControllerIntegrationTest {

    @SuppressWarnings("unchecked")
    @Test
    void itShouldFindLatestWithMessages() {
        var token = getAuthToken("TEMP-D", "user");
        
        var response = get("/message/summaries", token);
        assertEquals(200, response.getStatus());

        var result = fromResponse(response, List.class);
        assertEquals(2, result.size());

        var chatIds = result.stream().map(arg -> ((LinkedHashMap<String, String>) arg).get("chatId")).toList();
        assertThat(chatIds).containsExactlyInAnyOrder(
            "8b669447-ac5c-4a7d-ba40-e286d434fb78",
            "48bceef2-f02e-438e-b747-5435851bd14d"
        );
    }

}
