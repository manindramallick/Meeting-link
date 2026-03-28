package com.demo.aop;

import com.demo.exceptions.InvalidHeaderException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HeaderValidationAspect")
class HeaderValidationAspectTest {

    @InjectMocks private HeaderValidationAspect aspect;

    @Mock private ProceedingJoinPoint       joinPoint;
    @Mock private MethodSignature           methodSignature;
    @Mock private HttpServletRequest        httpRequest;
    @Mock private ServletRequestAttributes  requestAttributes;

    private MockedStatic<RequestContextHolder> requestContextHolderMock;

    // Dummy method used to supply MethodSignature.getMethod()
    @SuppressWarnings("unused")
    public void dummyMethod() {}

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        requestContextHolderMock = mockStatic(RequestContextHolder.class);
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                                .thenReturn(requestAttributes);

        // lenient: not used by WhenNoRequestAttributes nested class
        lenient().when(requestAttributes.getRequest()).thenReturn(httpRequest);

        Method method = HeaderValidationAspectTest.class.getMethod("dummyMethod");
        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getMethod()).thenReturn(method);
    }

    @AfterEach
    void tearDown() {
        requestContextHolderMock.close();
    }

    // ── Nested: no request attributes ────────────────────────────────────────
    @Nested
    @DisplayName("when RequestAttributes is null")
    class WhenNoRequestAttributes {

        @Test
        @DisplayName("should skip validation and proceed with joinPoint")
        void shouldProceedWhenNoAttributes() throws Throwable {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                                    .thenReturn(null);
            when(joinPoint.proceed()).thenReturn("result");

            Object result = aspect.validateHeaders(joinPoint);

            assertThat(result).isEqualTo("result");
            verify(joinPoint).proceed();
        }
    }

    // ── Nested: env header missing ───────────────────────────────────────────
    @Nested
    @DisplayName("when env header is missing or blank")
    class WhenEnvHeaderMissing {

        @Test
        @DisplayName("should throw InvalidHeaderException when env is null")
        void shouldThrowWhenEnvNull() {
            when(httpRequest.getHeader("env")).thenReturn(null);

            assertThatThrownBy(() -> aspect.validateHeaders(joinPoint))
                    .isInstanceOf(InvalidHeaderException.class)
                    .hasMessageContaining("missing or blank")
                    .satisfies(ex -> {
                        InvalidHeaderException ihe = (InvalidHeaderException) ex;
                        assertThat(ihe.getHeaderName()).isEqualTo("env");
                    });
        }

        @Test
        @DisplayName("should throw InvalidHeaderException when env is blank")
        void shouldThrowWhenEnvBlank() {
            when(httpRequest.getHeader("env")).thenReturn("   ");

            assertThatThrownBy(() -> aspect.validateHeaders(joinPoint))
                    .isInstanceOf(InvalidHeaderException.class)
                    .hasMessageContaining("missing or blank");
        }
    }

    // ── Nested: env header invalid value ─────────────────────────────────────
    @Nested
    @DisplayName("when env header value is not in ALLOWED_ENVIRONMENTS")
    class WhenEnvHeaderInvalid {

        @Test
        @DisplayName("should throw InvalidHeaderException for unknown env value")
        void shouldThrowForUnknownEnv() {
            when(httpRequest.getHeader("env")).thenReturn("staging");

            assertThatThrownBy(() -> aspect.validateHeaders(joinPoint))
                    .isInstanceOf(InvalidHeaderException.class)
                    .hasMessageContaining("Invalid value 'staging'")
                    .satisfies(ex -> {
                        InvalidHeaderException ihe = (InvalidHeaderException) ex;
                        assertThat(ihe.getHeaderName()).isEqualTo("env");
                        assertThat(ihe.getReceivedValue()).isEqualTo("staging");
                    });
        }

        @Test
        @DisplayName("should throw for 'production' as env value")
        void shouldThrowForProductionValue() {
            when(httpRequest.getHeader("env")).thenReturn("production");

            assertThatThrownBy(() -> aspect.validateHeaders(joinPoint))
                    .isInstanceOf(InvalidHeaderException.class)
                    .hasMessageContaining("Invalid value 'production'");
        }
    }

    // ── Nested: valid env values ──────────────────────────────────────────────
    @Nested
    @DisplayName("when env header has a valid value")
    class WhenEnvHeaderValid {

        @Test
        @DisplayName("should proceed and return result for 'dev'")
        void shouldProceedForDev() throws Throwable {
            when(httpRequest.getHeader("env")).thenReturn("dev");
            when(joinPoint.proceed()).thenReturn("token-result");

            Object result = aspect.validateHeaders(joinPoint);

            assertThat(result).isEqualTo("token-result");
            verify(joinPoint, times(1)).proceed();
        }

        @Test
        @DisplayName("should proceed for 'local'")
        void shouldProceedForLocal() throws Throwable {
            when(httpRequest.getHeader("env")).thenReturn("local");
            when(joinPoint.proceed()).thenReturn("ok");

            Object result = aspect.validateHeaders(joinPoint);
            assertThat(result).isEqualTo("ok");
        }

        @Test
        @DisplayName("should proceed for 'qa'")
        void shouldProceedForQa() throws Throwable {
            when(httpRequest.getHeader("env")).thenReturn("qa");
            when(joinPoint.proceed()).thenReturn("ok");

            Object result = aspect.validateHeaders(joinPoint);
            assertThat(result).isEqualTo("ok");
        }

        @Test
        @DisplayName("should proceed for 'prd'")
        void shouldProceedForPrd() throws Throwable {
            when(httpRequest.getHeader("env")).thenReturn("prd");
            when(joinPoint.proceed()).thenReturn("ok");

            Object result = aspect.validateHeaders(joinPoint);
            assertThat(result).isEqualTo("ok");
        }

        @Test
        @DisplayName("should be case-insensitive for env value")
        void shouldBeCaseInsensitive() throws Throwable {
            when(httpRequest.getHeader("env")).thenReturn("DEV");
            when(joinPoint.proceed()).thenReturn("ok");

            Object result = aspect.validateHeaders(joinPoint);
            assertThat(result).isEqualTo("ok");
        }
    }
}

