package com.queueless.security;

public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean isAuthorizedForTenant(Long targetOrgId, QueuelessUserPrincipal principal) {
        if (principal == null || targetOrgId == null) {
            return false;
        }
        if (principal.isSuperAdmin()) {
            Long scopedTenant = CURRENT_TENANT.get();
            if (scopedTenant != null) {
                return scopedTenant.equals(targetOrgId);
            }
            return true;
        }
        return targetOrgId.equals(principal.getOrganizationId());
    }
}
