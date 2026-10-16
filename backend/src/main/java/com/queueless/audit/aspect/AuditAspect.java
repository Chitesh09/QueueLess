package com.queueless.audit.aspect;

import com.queueless.audit.annotation.Auditable;
import com.queueless.audit.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import com.queueless.security.QueuelessUserPrincipal;
import com.queueless.security.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(com.queueless.audit.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            if (auditable != null) {
                String action = auditable.action();
                String entityType = auditable.entityType();

                Long orgId = TenantContext.getCurrentTenant();
                Long actorUserId = null;
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof QueuelessUserPrincipal principal) {
                    actorUserId = principal.getUserId();
                    if (orgId == null) {
                        orgId = principal.getOrganizationId();
                    }
                }

                auditLogService.recordAudit(orgId, action, entityType, null, actorUserId, "Method executed: " + method.getName());
            }
        } catch (Exception ignored) {}

        return result;
    }
}
