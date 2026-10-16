package com.queueless.security;

import com.queueless.common.constants.QueueConstants;
import com.queueless.common.util.MDCUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Long tenantId = null;
        String tenantHeader = request.getHeader(QueueConstants.HEADER_TENANT_ID);
        Long requestedTenantId = parseTenantHeader(tenantHeader);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof QueuelessUserPrincipal principal) {
            if (principal.isSuperAdmin() && requestedTenantId != null) {
                // SUPER_ADMIN can act on behalf of a specific tenant via X-Tenant-ID
                tenantId = requestedTenantId;
            } else {
                // All other roles strictly derive tenantId from their authenticated principal
                tenantId = principal.getOrganizationId();
            }
        } else {
            // Check direct JWT if filter chain order or context not yet set
            String bearerToken = getJwtFromRequest(request);
            if (StringUtils.hasText(bearerToken) && tokenProvider.validateToken(bearerToken)) {
                Long jwtTenantId = tokenProvider.getOrganizationIdFromToken(bearerToken);
                String role = tokenProvider.getRoleFromToken(bearerToken);
                if ("SUPER_ADMIN".equalsIgnoreCase(role) && requestedTenantId != null) {
                    tenantId = requestedTenantId;
                } else {
                    tenantId = jwtTenantId; // Trust JWT claim strictly
                }
            }
            // For unauthenticated callers, tenantId remains null.
            // NEVER trust client-supplied X-Tenant-ID to establish trusted tenant context for anonymous callers!
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

    private Long parseTenantHeader(String header) {
        if (StringUtils.hasText(header)) {
            try {
                return Long.parseLong(header.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
