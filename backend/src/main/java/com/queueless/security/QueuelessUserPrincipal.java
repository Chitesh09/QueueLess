package com.queueless.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class QueuelessUserPrincipal implements UserDetails {
    private final Long userId;
    private final Long organizationId;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public QueuelessUserPrincipal(Long userId, Long organizationId, String email, String password, String role, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.email = email;
        this.password = password != null ? password : "";
        this.role = role;
        this.authorities = authorities;
    }

    public QueuelessUserPrincipal(Long userId, Long organizationId, String email, String role, Collection<? extends GrantedAuthority> authorities) {
        this(userId, organizationId, email, "", role, authorities);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getRole() {
        return role;
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role) || hasRole("ROLE_SUPER_ADMIN");
    }

    public boolean isOrgAdmin() {
        return "ORG_ADMIN".equalsIgnoreCase(role) || hasRole("ROLE_ORG_ADMIN") || isSuperAdmin();
    }

    public boolean isBranchAdmin() {
        return "BRANCH_ADMIN".equalsIgnoreCase(role) || hasRole("ROLE_BRANCH_ADMIN") || isOrgAdmin();
    }

    public boolean isOperator() {
        return "OPERATOR".equalsIgnoreCase(role) || hasRole("ROLE_OPERATOR") || isBranchAdmin();
    }

    public boolean hasRole(String roleName) {
        if (authorities == null) return false;
        return authorities.stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(roleName));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
