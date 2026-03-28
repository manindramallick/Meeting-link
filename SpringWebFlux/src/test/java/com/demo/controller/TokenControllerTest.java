package com.demo.controller;

import com.demo.exceptions.GlobalExceptionHandler;
import com.demo.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import com.demo.aop.HeaderValidationAspect;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenController")
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Wrap controller with the real HeaderValidationAspect via proxy
        TokenController controller = new TokenController(tokenService);
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.addAspect(new HeaderValidationAspect());
        TokenController proxy = factory.getProxy();

        mockMvc = MockMvcBuilders
                .standaloneSetup(proxy)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Nested: valid requests ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /token — valid env header")
    class ValidRequests {

        @Test
        @DisplayName("should return 200 with generated token for 'dev'")
        void shouldReturn200ForDev() throws Exception {
            when(tokenService.generateToken("dev")).thenReturn("token_dev_xyz");

            mockMvc.perform(get("/token")
                            .header("env", "dev")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Generated token: token_dev_xyz"));

            verify(tokenService).generateToken("dev");
        }

        @Test
        @DisplayName("should return 200 for 'qa'")
        void shouldReturn200ForQa() throws Exception {
            when(tokenService.generateToken("qa")).thenReturn("token_qa_xyz");

            mockMvc.perform(get("/token").header("env", "qa"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Generated token: token_qa_xyz"));
        }

        @Test
        @DisplayName("should return 200 for 'prd'")
        void shouldReturn200ForPrd() throws Exception {
            when(tokenService.generateToken("prd")).thenReturn("token_prd_xyz");

            mockMvc.perform(get("/token").header("env", "prd"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 200 for 'local'")
        void shouldReturn200ForLocal() throws Exception {
            when(tokenService.generateToken("local")).thenReturn("token_local_xyz");

            mockMvc.perform(get("/token").header("env", "local"))
                    .andExpect(status().isOk());
        }
    }

    // ── Nested: missing env header ─────────────────────────────────────────
    @Nested
    @DisplayName("GET /token — missing or blank env header")
    class MissingEnvHeader {

        @Test
        @DisplayName("should return 400 when env header is absent")
        void shouldReturn400WhenEnvMissing() throws Exception {
            mockMvc.perform(get("/token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid Header"))
                    .andExpect(jsonPath("$.header").value("env"))
                    .andExpect(jsonPath("$.status").value(400));

            verify(tokenService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("should not call TokenService when env header is missing")
        void shouldNotCallServiceWhenEnvMissing() throws Exception {
            mockMvc.perform(get("/token"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(tokenService);
        }
    }

    // ── Nested: invalid env value ──────────────────────────────────────────
    @Nested
    @DisplayName("GET /token — invalid env header value")
    class InvalidEnvValue {

        @Test
        @DisplayName("should return 400 for unknown env 'staging'")
        void shouldReturn400ForStaging() throws Exception {
            mockMvc.perform(get("/token").header("env", "staging"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid Header"))
                    .andExpect(jsonPath("$.receivedValue").value("staging"))
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Invalid value 'staging'")));

            verifyNoInteractions(tokenService);
        }

        @Test
        @DisplayName("should return 400 for 'production' as env value")
        void shouldReturn400ForProduction() throws Exception {
            mockMvc.perform(get("/token").header("env", "production"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.receivedValue").value("production"));
        }

        @Test
        @DisplayName("should return 400 response body with timestamp")
        void shouldReturn400BodyWithTimestamp() throws Exception {
            mockMvc.perform(get("/token").header("env", "uat"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}

