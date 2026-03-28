package com.demo.filter;

import com.demo.config.TokenProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnvironmentFilter")
class EnvironmentFilterTest {

    @Mock private HttpServletRequest  request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain         filterChain;

    private EnvironmentFilter buildFilter(String defaultEnv) {
        TokenProperties props = new TokenProperties();
        props.setEnvironmentDefault(defaultEnv);
        props.setEnvironment(new TokenProperties.Environment("dev", "secret", "3600"));
        return new EnvironmentFilter(props);
    }

    // ── Nested: env header already present ───────────────────────────────────
    @Nested
    @DisplayName("when env header is present in request")
    class WhenEnvHeaderPresent {

        @Test
        @DisplayName("should pass the original request through unchanged")
        void shouldPassOriginalRequest() throws Exception {
            when(request.getHeader("env")).thenReturn("dev");
            buildFilter("local").doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not wrap or modify the request")
        void shouldNotWrapRequest() throws Exception {
            when(request.getHeader("env")).thenReturn("qa");
            buildFilter("local").doFilterInternal(request, response, filterChain);

            ArgumentCaptor<HttpServletRequest> captor =
                    ArgumentCaptor.forClass(HttpServletRequest.class);
            verify(filterChain).doFilter(captor.capture(), eq(response));
            assertThat(captor.getValue()).isSameAs(request);
        }
    }

    // ── Nested: env header missing ───────────��────────────────────────────────
    @Nested
    @DisplayName("when env header is missing")
    class WhenEnvHeaderMissing {

        @Test
        @DisplayName("should inject environmentDefault into wrapped request")
        void shouldInjectDefaultEnv() throws Exception {
            when(request.getHeader("env")).thenReturn(null);
            buildFilter("local").doFilterInternal(request, response, filterChain);

            ArgumentCaptor<HttpServletRequest> captor =
                    ArgumentCaptor.forClass(HttpServletRequest.class);
            verify(filterChain).doFilter(captor.capture(), eq(response));
            assertThat(captor.getValue()).isNotSameAs(request);
            assertThat(captor.getValue().getHeader("env")).isEqualTo("local");
        }

        @Test
        @DisplayName("should inject default when env header is blank")
        void shouldInjectDefaultWhenBlank() throws Exception {
            when(request.getHeader("env")).thenReturn("   ");
            buildFilter("local").doFilterInternal(request, response, filterChain);

            ArgumentCaptor<HttpServletRequest> captor =
                    ArgumentCaptor.forClass(HttpServletRequest.class);
            verify(filterChain).doFilter(captor.capture(), eq(response));
            assertThat(captor.getValue().getHeader("env")).isEqualTo("local");
        }

        @Test
        @DisplayName("should use custom environmentDefault value from config")
        void shouldUseCustomDefault() throws Exception {
            when(request.getHeader("env")).thenReturn(null);
            buildFilter("qa").doFilterInternal(request, response, filterChain);

            ArgumentCaptor<HttpServletRequest> captor =
                    ArgumentCaptor.forClass(HttpServletRequest.class);
            verify(filterChain).doFilter(captor.capture(), eq(response));
            assertThat(captor.getValue().getHeader("env")).isEqualTo("qa");
        }
    }

    // ── Nested: filter chain is always called ─────────────────────────────────
    @Nested
    @DisplayName("filter chain invocation")
    class FilterChainInvocation {

        @Test
        @DisplayName("should always call filterChain.doFilter exactly once")
        void shouldAlwaysCallFilterChain() throws Exception {
            when(request.getHeader("env")).thenReturn("prd");
            buildFilter("local").doFilterInternal(request, response, filterChain);
            verify(filterChain, times(1)).doFilter(any(), eq(response));
        }
    }
}

