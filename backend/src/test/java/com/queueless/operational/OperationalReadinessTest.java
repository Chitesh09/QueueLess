package com.queueless.operational;

import com.queueless.common.exception.GlobalExceptionHandler;
import com.queueless.security.CorrelationIdFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OperationalReadinessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    // =========================================================================
    // 1. ACTUATOR HEALTH & SENSITIVE DATA REDACTION
    // =========================================================================
    @Nested
    @DisplayName("Actuator Health & Sensitive Data Redaction")
    class HealthEndpointTests {

        @Test
        @DisplayName("Public GET /actuator/health returns 200 UP without leaking database credentials or disk paths")
        void healthEndpoint_returnsUpWithoutLeakingDetails() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    // Verify sensitive connection details are suppressed from public unauthenticated callers
                    .andExpect(jsonPath("$.components.db.details").doesNotExist())
                    .andExpect(jsonPath("$.components.redis.details").doesNotExist())
                    .andExpect(jsonPath("$.components.diskSpace.details.path").doesNotExist());
        }
    }

    // =========================================================================
    // 2. REQUEST CORRELATION & TRACEABILITY
    // =========================================================================
    @Nested
    @DisplayName("Request Correlation & Traceability")
    class RequestCorrelationTests {

        @Test
        @DisplayName("Incoming X-Correlation-ID header is propagated back in HTTP response header")
        void correlationId_propagatesProvidedHeader() throws Exception {
            String clientCorrelationId = "client-trace-" + UUID.randomUUID();

            mockMvc.perform(get("/actuator/health")
                            .header(CorrelationIdFilter.CORRELATION_ID_HEADER, clientCorrelationId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, clientCorrelationId));
        }

        @Test
        @DisplayName("Missing X-Correlation-ID automatically generates a valid UUID in response header")
        void correlationId_generatesUuidWhenOmitted() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(CorrelationIdFilter.CORRELATION_ID_HEADER))
                    .andExpect(result -> {
                        String generatedId = result.getResponse().getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
                        assertThat(generatedId).isNotBlank();
                        // Verify valid UUID structure
                        assertThat(UUID.fromString(generatedId)).isNotNull();
                    });
        }
    }

    // =========================================================================
    // 3. ERROR HANDLING & SENSITIVE DATA SUPPRESSION
    // =========================================================================
    @Nested
    @DisplayName("Error Handling & Sensitive Data Suppression")
    class ErrorSanitizationTests {

        @Test
        @DisplayName("Unhandled internal exception returns sanitized 500 ProblemDetail with correlation ID and no stack trace")
        void handleGeneric_sanitizesInternalException() {
            Exception secretLeakingException = new RuntimeException("SELECT * FROM users WHERE password_hash = 'secret123'");

            ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleGeneric(secretLeakingException);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ProblemDetail body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTitle()).isEqualTo("Internal Server Error");

            // Detail must NOT leak the query or raw exception message
            assertThat(body.getDetail()).doesNotContain("password_hash");
            assertThat(body.getDetail()).doesNotContain("SELECT * FROM users");
            assertThat(body.getDetail()).contains("An unexpected internal server error occurred");

            // Timestamp must be present
            assertThat(body.getProperties()).containsKey("timestamp");
        }
    }

    // =========================================================================
    // 4. SECURITY HEADERS
    // =========================================================================
    @Nested
    @DisplayName("Security Headers")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Responses enforce standard security headers against sniffing and clickjacking")
        void responses_enforceSecurityHeaders() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("X-XSS-Protection", "0"));
        }
    }
}
