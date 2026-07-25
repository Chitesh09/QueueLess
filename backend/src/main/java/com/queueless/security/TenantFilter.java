package com.queueless.security;

import com.queueless.common.constants.QueueConstants;
import com.queueless.common.util.MDCUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public TenantFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String correlationId = request.getHeader(QueueConstants.HEADER_CORRELATION_ID);
        MDCUtil.setCorrelationId(correlationId);

        String tenantHeader = request.getHeader(QueueConstants.HEADER_TENANT_ID);
        Long tenantId = null;

        if (StringUtils.hasText(tenantHeader)) {
            try {
                tenantId = Long.parseLong(tenantHeader);
            } catch (NumberFormatException ignored) {}
        }

        String bearerToken = getJwtFromRequest(request);
        if (StringUtils.hasText(bearerToken) && tokenProvider.validateToken(bearerToken)) {
            Long jwtTenantId = tokenProvider.getOrganizationIdFromToken(bearerToken);
            if (jwtTenantId != null) {
                tenantId = jwtTenantId; // Trust JWT claim over spoofable header
            }
        }

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            MDCUtil.setTenantId(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDCUtil.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
