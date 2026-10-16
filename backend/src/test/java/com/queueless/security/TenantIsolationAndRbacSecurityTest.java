package com.queueless.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueless.common.constants.QueueConstants;
import com.queueless.domain.entity.Counter;
import com.queueless.audit.service.AuditLogService;
import com.queueless.domain.entity.AuditLog;
import com.queueless.domain.repository.AuditLogRepository;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TenantIsolationAndRbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private String superAdminToken;
    private String org1AdminToken;
    private String org1OperatorToken;
    private String org1CustomerToken;
    private String org2AdminToken;
    private String org2OperatorToken;

    @BeforeEach
    void setUp() {
        // Users from seed data:
        // User 1: superadmin@queueless.com (SUPER_ADMIN)
        // User 2: admin@cityhospital.com (ORG_ADMIN, Org 1)
        // User 3: operator@cityhospital.com (OPERATOR, Org 1)
        // User 4: john.doe@gmail.com (USER, Org 1)
        // Org 2 test users (simulated):
        // User 20: bankadmin@metrobank.com (ORG_ADMIN, Org 2)
        // User 21: bankoperator@metrobank.com (OPERATOR, Org 2)

        superAdminToken = tokenProvider.generateToken("superadmin@queueless.com", 1L, 1L, "SUPER_ADMIN");
        org1AdminToken = tokenProvider.generateToken("admin@cityhospital.com", 2L, 1L, "ORG_ADMIN");
        org1OperatorToken = tokenProvider.generateToken("operator@cityhospital.com", 3L, 1L, "OPERATOR");
        org1CustomerToken = tokenProvider.generateToken("john.doe@gmail.com", 4L, 1L, "USER");

        org2AdminToken = tokenProvider.generateToken("bankadmin@metrobank.com", 20L, 2L, "ORG_ADMIN");
        org2OperatorToken = tokenProvider.generateToken("bankoperator@metrobank.com", 21L, 2L, "OPERATOR");

        // Ensure Counter 1 (Org 1) and Counter 3 (Org 2) are ONLINE
        counterRepository.findById(1L).ifPresent(c -> {
            c.setStatus("ONLINE");
            counterRepository.save(c);
        });
        counterRepository.findById(3L).ifPresent(c -> {
            c.setStatus("ONLINE");
            counterRepository.save(c);
        });

        tokenRepository.deleteAll();
    }

    // =========================================================================
    // 1. PUBLIC VS PRIVATE ENDPOINTS
    // =========================================================================
    @Nested
    @DisplayName("Public vs Private Endpoints")
    class PublicVsPrivateEndpoints {

        @Test
        @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
        void unauthenticatedAccessToProtected_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/organizations"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/counters/1/queue"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/counters/1/call-next"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/analytics/summary"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Genuinely public endpoints remain accessible without authentication")
        void publicEndpoints_remainAccessibleWithoutAuth() throws Exception {
            // Guest join queue
            mockMvc.perform(post("/api/v1/services/1/tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"priorityClass\":\"STANDARD\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tokenNumber").isNotEmpty());

            // Public display board snapshot
            mockMvc.perform(get("/api/v1/branches/1/display"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.branchId").value(1));

            // Public service catalog
            mockMvc.perform(get("/api/v1/branches/1/services"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Public departments
            mockMvc.perform(get("/api/v1/branches/1/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Dynamic ETA calculation
            mockMvc.perform(get("/api/v1/services/1/eta").param("position", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.serviceId").value(1));
        }
    }

    // =========================================================================
    // 2. CROSS-TENANT ISOLATION
    // =========================================================================
    @Nested
    @DisplayName("Cross-Tenant Isolation")
    class CrossTenantIsolation {

        @Test
        @DisplayName("Org 1 Admin cannot read Org 2 organization details (returns 403)")
        void org1Admin_cannotAccessOrg2_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/organizations/2")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Admin cannot list Org 2 branches (returns 403)")
        void org1Admin_cannotListOrg2Branches_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/organizations/2/branches")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Admin cannot create a branch in Org 2 (returns 403)")
        void org1Admin_cannotCreateBranchInOrg2_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/organizations/2/branches")
                            .header("Authorization", "Bearer " + org1AdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Illegitimate Branch\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Operator cannot view queue for Counter 3 in Org 2 (returns 403)")
        void org1Operator_cannotViewOrg2CounterQueue_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/counters/3/queue")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Operator cannot call next token on Counter 3 in Org 2 (returns 403)")
        void org1Operator_cannotCallNextOnOrg2Counter_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/counters/3/call-next")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Operator cannot pause Counter 3 in Org 2 (returns 403)")
        void org1Operator_cannotPauseOrg2Counter_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/counters/3/pause")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Operator cannot resume Counter 3 in Org 2 (returns 403)")
        void org1Operator_cannotResumeOrg2Counter_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/counters/3/resume")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Admin cannot create counter in Branch 2 of Org 2 (returns 403)")
        void org1Admin_cannotCreateCounterInOrg2Branch_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/branches/2/counters")
                            .header("Authorization", "Bearer " + org1AdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Malicious Counter\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org 1 Staff cannot access analytics overview for Branch 2 in Org 2 (returns 403)")
        void org1Staff_cannotAccessOrg2Analytics_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/analytics/branches/2/overview")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/analytics/summary")
                            .param("branchId", "2")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // 3. FORGED HEADER PREVENTION
    // =========================================================================
    @Nested
    @DisplayName("Forged Tenant Header Prevention")
    class ForgedHeaderPrevention {

        @Test
        @DisplayName("Org 1 Admin sending forged X-Tenant-ID: 2 is still rejected with 403")
        void org1Admin_withSpoofedTenantHeader_isDenied() throws Exception {
            // Attacker possesses valid Org 1 JWT but injects X-Tenant-ID: 2 header
            mockMvc.perform(get("/api/v1/organizations/2")
                            .header("Authorization", "Bearer " + org1AdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "2"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/counters/3/queue")
                            .header("Authorization", "Bearer " + org1OperatorToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated caller sending X-Tenant-ID header is still rejected with 401")
        void unauthenticated_withTenantHeader_isRejectedWith401() throws Exception {
            mockMvc.perform(get("/api/v1/organizations")
                            .header(QueueConstants.HEADER_TENANT_ID, "1"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/counters/1/queue")
                            .header(QueueConstants.HEADER_TENANT_ID, "1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // 4. ROLE-BASED ACCESS CONTROL (RBAC) & LEAST PRIVILEGE
    // =========================================================================
    @Nested
    @DisplayName("Role-Based Access Control (RBAC)")
    class RoleBasedAccessControl {

        @Test
        @DisplayName("Customer (ROLE_USER) cannot execute operator counter operations (returns 403)")
        void customer_cannotExecuteCounterOperations_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/counters/1/call-next")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/counters/1/queue")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/counters/1/pause")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Customer (ROLE_USER) cannot create organizations or branches (returns 403)")
        void customer_cannotCreateOrgOrBranch_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/organizations")
                            .header("Authorization", "Bearer " + org1CustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Customer Org\"}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/organizations/1/branches")
                            .header("Authorization", "Bearer " + org1CustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Customer Branch\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Customer (ROLE_USER) cannot access analytics reports (returns 403)")
        void customer_cannotAccessAnalytics_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/analytics/summary")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/analytics/branches/1/overview")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Operator (ROLE_OPERATOR) cannot create organizations or branches (returns 403)")
        void operator_cannotCreateOrgOrBranch_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/organizations")
                            .header("Authorization", "Bearer " + org1OperatorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Operator Org\"}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/organizations/1/branches")
                            .header("Authorization", "Bearer " + org1OperatorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Operator Branch\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Org Admin (ROLE_ORG_ADMIN) cannot create new organizations (returns 403, requires SUPER_ADMIN)")
        void orgAdmin_cannotCreateNewOrganization_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/organizations")
                            .header("Authorization", "Bearer " + org1AdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Unauthorized Org Creation\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Customer (ROLE_USER) cannot access organization system preferences (returns 403)")
        void customer_cannotAccessPreferences_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/organization/preferences")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // 5. LEGITIMATE TENANT & ROLE OPERATIONS
    // =========================================================================
    @Nested
    @DisplayName("Legitimate Tenant Operations")
    class LegitimateTenantOperations {

        @Test
        @DisplayName("Org 1 Admin can access Org 1 organization details")
        void org1Admin_canAccessOwnOrg() throws Exception {
            mockMvc.perform(get("/api/v1/organizations/1")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("City Care Hospital"));
        }

        @Test
        @DisplayName("Org 1 Admin can list and create branches in Org 1")
        void org1Admin_canManageOwnBranches() throws Exception {
            mockMvc.perform(get("/api/v1/organizations/1/branches")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(post("/api/v1/organizations/1/branches")
                            .header("Authorization", "Bearer " + org1AdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"City Care North Campus\",\"address\":\"500 North Blvd\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.organizationId").value(1));
        }

        @Test
        @DisplayName("Org 1 Operator can view queue and pause/resume own counter")
        void org1Operator_canOperateOwnCounter() throws Exception {
            mockMvc.perform(get("/api/v1/counters/1/queue")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(post("/api/v1/counters/1/pause")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("PAUSED"));

            mockMvc.perform(post("/api/v1/counters/1/resume")
                            .header("Authorization", "Bearer " + org1OperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("ONLINE"));
        }

        @Test
        @DisplayName("SUPER_ADMIN can access cross-tenant organizations and create new orgs")
        void superAdmin_hasGlobalAccess() throws Exception {
            // Super admin views all orgs
            mockMvc.perform(get("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            // Super admin views Org 2
            mockMvc.perform(get("/api/v1/organizations/2")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Metro National Bank"));

            // Super admin creates new org
            mockMvc.perform(post("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Apex Logistics Corp\",\"planTier\":\"ENTERPRISE\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("Apex Logistics Corp"));
        }

        @Test
        @DisplayName("Authenticated user gets their real profile from DB at /users/me")
        void authenticatedUser_getsRealProfileWithoutHardcodedMock() throws Exception {
            // Test with John Customer (User ID 4 from seed data)
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(4))
                    .andExpect(jsonPath("$.data.email").value("john.doe@gmail.com"))
                    .andExpect(jsonPath("$.data.name").value("John Customer"))
                    .andExpect(jsonPath("$.data.role").value("USER"));

            // Test with Hospital Admin (User ID 2 from seed data)
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(2))
                    .andExpect(jsonPath("$.data.email").value("admin@cityhospital.com"))
                    .andExpect(jsonPath("$.data.name").value("Hospital Admin"))
                    .andExpect(jsonPath("$.data.role").value("ORG_ADMIN"));
        }

        @Test
        @DisplayName("Org Admin can view system preferences")
        void orgAdmin_canAccessPreferences() throws Exception {
            mockMvc.perform(get("/api/v1/organization/preferences")
                            .header("Authorization", "Bearer " + org1AdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.enableSmsNotifications").value(true));
        }
    }

    // =========================================================================
    // 6. SUPER-ADMIN TENANT SCOPING
    // =========================================================================
    @Nested
    @DisplayName("Super-Admin Tenant Scoping")
    class SuperAdminScoping {

        @Test
        @DisplayName("SUPER_ADMIN scoped to Org 2 with X-Tenant-ID: 2 can access Org 2 but is denied Org 1 (returns 403)")
        void superAdmin_withScopedTenantHeader_restrictedToTargetTenant() throws Exception {
            // Can access Org 2
            mockMvc.perform(get("/api/v1/organizations/2")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(2));

            // Denied Org 1
            mockMvc.perform(get("/api/v1/organizations/1")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SUPER_ADMIN with X-Tenant-ID: 2 gets only Org 2 when listing organizations")
        void superAdmin_withScopedTenantHeader_listsOnlyTargetOrg() throws Exception {
            mockMvc.perform(get("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value(2));
        }

        @Test
        @DisplayName("SUPER_ADMIN without X-Tenant-ID has global unscoped access across all organizations")
        void superAdmin_withoutTenantHeader_hasGlobalAccess() throws Exception {
            mockMvc.perform(get("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

            mockMvc.perform(get("/api/v1/organizations/1")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/organizations/2")
                            .header("Authorization", "Bearer " + superAdminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SUPER_ADMIN with malformed X-Tenant-ID header defaults safely to global scope without 500 error")
        void superAdmin_withMalformedTenantHeader_handlesSafely() throws Exception {
            mockMvc.perform(get("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "malformed_abc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("SUPER_ADMIN with non-existent X-Tenant-ID returns empty list without leaking cross-tenant data")
        void superAdmin_withNonExistentTenantHeader_returnsEmptyList() throws Exception {
            mockMvc.perform(get("/api/v1/organizations")
                            .header("Authorization", "Bearer " + superAdminToken)
                            .header(QueueConstants.HEADER_TENANT_ID, "99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // =========================================================================
    // 7. TOKEN PRIVACY & SANITIZATION
    // =========================================================================
    @Nested
    @DisplayName("Token Privacy & Sanitization")
    class TokenPrivacyAndSanitization {

        @Test
        @DisplayName("Token owner retrieving token receives full details including qrSignature and userId")
        void owner_retrievingToken_receivesFullDetailsWithQrSignature() throws Exception {
            // User 4 joins queue for Service 1
            String responseBody = mockMvc.perform(post("/api/v1/services/1/tokens")
                            .header("Authorization", "Bearer " + org1CustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"priorityClass\":\"STANDARD\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            long tokenId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

            // Owner retrieves token
            mockMvc.perform(get("/api/v1/tokens/" + tokenId)
                            .header("Authorization", "Bearer " + org1CustomerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(tokenId))
                    .andExpect(jsonPath("$.data.userId").value(4))
                    .andExpect(jsonPath("$.data.qrSignature").isNotEmpty());
        }

        @Test
        @DisplayName("Unrelated user retrieving token receives sanitized response with qrSignature and userId suppressed")
        void unrelatedUser_retrievingToken_receivesSanitizedResponse() throws Exception {
            // User 4 joins queue for Service 1
            String responseBody = mockMvc.perform(post("/api/v1/services/1/tokens")
                            .header("Authorization", "Bearer " + org1CustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"priorityClass\":\"STANDARD\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            long tokenId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

            // Org 2 user retrieves User 4's token
            mockMvc.perform(get("/api/v1/tokens/" + tokenId)
                            .header("Authorization", "Bearer " + org2AdminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(tokenId))
                    .andExpect(jsonPath("$.data.userId").isEmpty())
                    .andExpect(jsonPath("$.data.userName").isEmpty())
                    .andExpect(jsonPath("$.data.qrSignature").isEmpty());
        }

        @Test
        @DisplayName("Anonymous caller without matching guest session receives sanitized response with qrSignature suppressed")
        void anonymousCaller_withoutGuestSession_receivesSanitizedResponse() throws Exception {
            // User 4 joins queue for Service 1
            String responseBody = mockMvc.perform(post("/api/v1/services/1/tokens")
                            .header("Authorization", "Bearer " + org1CustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"priorityClass\":\"STANDARD\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            long tokenId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

            // Anonymous caller retrieves token
            mockMvc.perform(get("/api/v1/tokens/" + tokenId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(tokenId))
                    .andExpect(jsonPath("$.data.userId").isEmpty())
                    .andExpect(jsonPath("$.data.userName").isEmpty())
                    .andExpect(jsonPath("$.data.qrSignature").isEmpty());
        }
    }

    // =========================================================================
    // 8. ASYNCHRONOUS AUDIT LOGGING CONTEXT PRESERVATION
    // =========================================================================
    @Nested
    @DisplayName("Asynchronous Audit Logging Context Preservation")
    class AsynchronousAuditLogging {

        @Test
        @DisplayName("Audit logging preserves organizationId and actorUserId across asynchronous invocation")
        void auditLogging_preservesOrganizationAndActor() throws Exception {
            // Directly invoke audit log service
            auditLogService.recordAudit(1L, "SECURITY_TEST_ACTION", "Organization", 1L, 2L, "{\"detail\":\"audit-security-gate\"}");

            // Allow async thread execution
            Thread.sleep(200);

            List<AuditLog> logs = auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(1L);
            assertThat(logs).isNotEmpty();

            AuditLog latestLog = logs.get(0);
            assertThat(latestLog.getOrganizationId()).isEqualTo(1L);
            assertThat(latestLog.getAction()).isEqualTo("SECURITY_TEST_ACTION");
            assertThat(latestLog.getActorUserId()).isEqualTo(2L);
            assertThat(latestLog.getEntityType()).isEqualTo("Organization");

            // Verify Org 2 has not received Org 1's audit entry
            List<AuditLog> org2Logs = auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(2L);
            boolean org2ContainsLog = org2Logs.stream().anyMatch(l -> "SECURITY_TEST_ACTION".equals(l.getAction()));
            assertThat(org2ContainsLog).isFalse();
        }
    }
}
