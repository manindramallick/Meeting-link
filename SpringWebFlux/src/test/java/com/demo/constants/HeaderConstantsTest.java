package com.demo.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HeaderConstants")
class HeaderConstantsTest {

    @Test
    @DisplayName("ENV_HEADER constant should be 'env'")
    void envHeaderShouldBeEnv() {
        assertThat(HeaderConstants.ENV_HEADER).isEqualTo("env");
    }

    @Test
    @DisplayName("CORRELATION_ID constant should be 'X-Correlation-Id'")
    void correlationIdShouldBeCorrect() {
        assertThat(HeaderConstants.CORRELATION_ID).isEqualTo("X-Correlation-Id");
    }

    @Test
    @DisplayName("ALLOWED_ENVIRONMENTS should contain exactly local, dev, qa, prd")
    void allowedEnvironmentsShouldContainExpectedValues() {
        assertThat(HeaderConstants.ALLOWED_ENVIRONMENTS)
                .hasSize(4)
                .containsExactlyInAnyOrder("local", "dev", "qa", "prd");
    }

    @Test
    @DisplayName("ALLOWED_ENVIRONMENTS should not contain unexpected values")
    void allowedEnvironmentsShouldNotContainUnknownValues() {
        assertThat(HeaderConstants.ALLOWED_ENVIRONMENTS)
                .doesNotContain("staging", "uat", "prod", "production");
    }
}

