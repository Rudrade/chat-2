package dev.rudrade.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.jdbc.Sql;

import dev.rudrade.chat.ControllerIntegrationTest;
import dev.rudrade.chat.dto.UserDto;
import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;

import static org.assertj.core.api.Assertions.*;

@Sql("/sql-scripts/users.sql")
class UserControllerTest extends ControllerIntegrationTest {

    //==========
    //  login
    //==========

    @Test
    void itShouldLogin() {
        var request = new LoginRequest("user-test", "user");

        var response = post("/user/login", request);
        assertEquals(200, response.getStatus());
        
        var result = fromResponse(response, LoginResponse.class);
        assertNotNull(result);
        assertThat(result.token()).isNotBlank();
    }

    @Test
    void itShouldReturn400WithInvalidParams() {
        var request = new LoginRequest("    ", null);

        var response = post("/user/login", request);
        assertEquals(400, response.getStatus());
        
        var result = fromResponse(response, Error.class);
        assertNotNull(result);
        assertThat(result.errors())
            .isNotNull()
            .hasSize(2);
    }

    @Test
    void itShouldReturn401WhenUserNotFound() {
        var request = new LoginRequest("user-test", "invalid");

        var response = post("/user/login", request);
        assertEquals(401, response.getStatus());
    }

    //===============
    //  getDetails
    //===============

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30", // Invalid
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjaGF0YXBwIiwic3ViIjoiMjlhOGQ5NjAtNDZkMi00ZTU1LTgwYWItN2Y2NDc3NTQxYTczIiwiaWF0IjoxNDIwMDcwNDAwLCJleHAiOjE0MjAwNzA0MDB9.Uu53P1zZ68t5HaqF1rDXRg2_6LoRccrIziTHDQSksa8" // Expired
    })
    void itShouldReturn401WithInvalidToken(String token) {
        var response = get("/user/details", token);
        assertEquals(401, response.getStatus());
    }
    
    @Test
    void itShouldGetDetails() {
        var response = get("/user/details", getAuthToken());
        assertEquals(200, response.getStatus());

        var result = fromResponse(response, UserDto.class);
        assertNotNull(result);
        assertEquals("29a8d960-46d2-4e55-80ab-7f6477541a28", result.id().toString());
        assertEquals("user-test", result.username());
        assertEquals("test", result.name());
    }
}
