package dev.rudrade.chat;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Sql("/sql-scripts/users.sql")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerIntegrationTest extends SqlIntegrationTest {

    @Autowired protected MockMvcTester mvcTester;
    private String authToken;
    
    protected String getAuthToken() {
        if (authToken != null) return authToken;
        
        try {
            var request = new LoginRequest("user-test", "user");
            var result = post("/user/login", request);
            var response = fromResponse(result, LoginResponse.class);
            authToken = response.token();
            
            return authToken;
        } catch (Exception ex) {
            fail(ex);
            return null;
        }
    }
    
    private final ObjectMapper mapper = new ObjectMapper();
    protected String json(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            fail(e);
            return null;
        }
    }

    protected <T> T fromResponse(MockHttpServletResponse response, Class<T> type) {
        try {
            return mapper.readValue(response.getContentAsByteArray(), type);
        } catch (Exception ex) {
            fail(ex);
            return null;
        }
    }

    protected MockHttpServletResponse post(String uri, Object body) {
        return mvcTester.post().uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body))
            .exchange().getResponse();
    }

    protected MockHttpServletResponse get(String uri, String authToken) {
        return mvcTester.get().uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer "+authToken)
            .exchange().getResponse();
    }

    protected record Error(String message, Set<String> errors) {}
}
