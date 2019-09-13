package net.sunxu.website.auth.service;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sunxu.website.test.helputil.authtoken.AuthTokenBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class AbstractTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String getToken(String... roles) {
        var builder = new AuthTokenBuilder();
        builder.id(100L)
                .name("auth-service")
                .exipreSeconds(1000L)
                .issuer("test-issuer");
        for (String role : roles) {
            if ("service".equalsIgnoreCase(role)) {
                builder.service(true);
            }
            builder.addRole(role);
        }
        return builder.build();
    }

    protected <T> T restful(MockHttpServletRequestBuilder builder, Class<T> expectedType)
            throws Exception {
        String json = mockMvc.perform(builder
                .header("Authorization", getToken("service", "auth"))
                .header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(json, expectedType);
    }
}
