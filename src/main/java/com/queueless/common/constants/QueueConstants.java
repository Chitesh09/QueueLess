package com.queueless.common.constants;

public class QueueConstants {
    public static final String HEADER_TENANT_ID = "X-Tenant-ID";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    public static final String WS_TOPIC_QUEUE = "/topic/queue/";
    public static final String WS_TOPIC_TOKEN = "/topic/tokens/";
    public static final String WS_USER_QUEUE = "/user/queue/token-updates";

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ORG_ADMIN = "ORG_ADMIN";
    public static final String ROLE_BRANCH_ADMIN = "BRANCH_ADMIN";
    public static final String ROLE_OPERATOR = "OPERATOR";
    public static final String ROLE_USER = "USER";
}
