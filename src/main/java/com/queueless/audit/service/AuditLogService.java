package com.queueless.audit.service;

import com.queueless.domain.entity.AuditLog;
import com.queueless.domain.repository.AuditLogRepository;
import com.queueless.security.TenantContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void recordAudit(String action, String entityType, Long entityId, Long actorUserId, String metadataJson) {
        AuditLog log = AuditLog.builder()
                .organizationId(TenantContext.getCurrentTenant())
                .actorUserId(actorUserId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadataJson(metadataJson)
                .build();
        auditLogRepository.save(log);
    }
}
