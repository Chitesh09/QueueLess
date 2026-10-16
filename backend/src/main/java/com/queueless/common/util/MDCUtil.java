package com.queueless.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public class MDCUtil {

    public static final String CORRELATION_ID = "correlationId";
    public static final String TENANT_ID = "tenantId";

    public static void setCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID, correlationId);
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    public static void setTenantId(Long tenantId) {
        if (tenantId != null) {
            MDC.put(TENANT_ID, String.valueOf(tenantId));
        } else {
            MDC.put(TENANT_ID, "GLOBAL");
        }
    }

    public static void clearTenantId() {
        MDC.remove(TENANT_ID);
    }

    public static void clear() {
        MDC.clear();
    }
}
