package com.demo.service;

import com.demo.config.TokenProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenService")
class TokenServiceTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Build a TokenService where environment.name IS populated. */
    private TokenService serviceWithName(String name) {
        TokenProperties.Environment env =
                new TokenProperties.Environment(name, "test-secret", "3600000");
        TokenProperties props = new TokenProperties();
        props.setEnvironmentDefault("local");
        props.setEnvironment(env);
        return new TokenService(props);
    }

    /** Build a TokenService where environment.name IS empty/null. */
    private TokenService serviceWithoutName() {
        TokenProperties.Environment env =
                new TokenProperties.Environment("", "test-secret", "3600000");
        TokenProperties props = new TokenProperties();
        props.setEnvironmentDefault("local");
        props.setEnvironment(env);
        return new TokenService(props);
    }

    // ── Branch 1: name is present → token uses name ───────────────────────────
    @Nested
    @DisplayName("when environment name is configured")
    class WhenNameIsPresent {

        @Test
        @DisplayName("should return token starting with 'token_'")
        void shouldStartWithTokenPrefix() {
            String token = serviceWithName("dev").generateToken("dev");
            assertThat(token).startsWith("token_");
        }

        @Test
        @DisplayName("should follow pattern token_<name>_<secret>_<expiration>")
        void shouldFollowNamePattern() {
            String token = serviceWithName("dev").generateToken("dev");
            assertThat(token).isEqualTo("token_dev_test-secret_3600000");
        }

        @Test
        @DisplayName("should contain secret and expiration")
        void shouldContainSecretAndExpiration() {
            String token = serviceWithName("dev").generateToken("qa");
            assertThat(token)
                    .contains("test-secret")
                    .contains("3600000");
        }

        @Test
        @DisplayName("should use name from config — not the passed env argument")
        void shouldUseConfigNameNotPassedEnv() {
            // name="dev" in config; passing "qa" as env → token still uses name "dev"
            String token = serviceWithName("dev").generateToken("qa");
            assertThat(token).isEqualTo("token_dev_test-secret_3600000");
        }

        @Test
        @DisplayName("should produce same token regardless of env argument when name is set")
        void shouldProduceSameTokenForDifferentEnvArgs() {
            TokenService svc = serviceWithName("dev");
            assertThat(svc.generateToken("qa"))
                    .isEqualTo(svc.generateToken("prd"))
                    .isEqualTo(svc.generateToken("local"));
        }
    }

    // ── Branch 2: name is empty → token uses passed env ───────────────────────
    @Nested
    @DisplayName("when environment name is empty")
    class WhenNameIsEmpty {

        @Test
        @DisplayName("should follow pattern token_<env>_<secret>_<expiration>")
        void shouldFollowEnvPattern() {
            String token = serviceWithoutName().generateToken("qa");
            assertThat(token).isEqualTo("token_qa_test-secret_3600000");
        }

        @Test
        @DisplayName("should embed the passed env argument in the token")
        void shouldEmbedPassedEnv() {
            assertThat(serviceWithoutName().generateToken("dev")).contains("dev");
            assertThat(serviceWithoutName().generateToken("qa")).contains("qa");
            assertThat(serviceWithoutName().generateToken("prd")).contains("prd");
        }

        @Test
        @DisplayName("should generate different tokens for different env values")
        void shouldGenerateDifferentTokensForDifferentEnv() {
            TokenService svc = serviceWithoutName();
            assertThat(svc.generateToken("dev")).isNotEqualTo(svc.generateToken("qa"));
        }

        @Test
        @DisplayName("should contain secret and expiration")
        void shouldContainSecretAndExpiration() {
            String token = serviceWithoutName().generateToken("dev");
            assertThat(token)
                    .contains("test-secret")
                    .contains("3600000");
        }
    }
}
