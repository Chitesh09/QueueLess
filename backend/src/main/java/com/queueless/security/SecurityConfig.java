package com.queueless.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;

    public SecurityConfig(CorrelationIdFilter correlationIdFilter,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantFilter tenantFilter) {
        this.correlationIdFilter = correlationIdFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/ws/**",
                                "/ws-queue/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Public queue operations: joining queue, dynamic ETA calculation, service/dept catalog, waiting room display, QR check-in, tracking token
                        .requestMatchers(HttpMethod.POST, "/api/v1/services/*/tokens").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/services/*/eta").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/branches/*/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/branches/*/departments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/branches/*/display").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tokens/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tokens/*/check-in").permitAll()

                        // Counter operator operations: OPERATOR, BRANCH_ADMIN, ORG_ADMIN, SUPER_ADMIN
                        .requestMatchers("/api/v1/counters/**").hasAnyRole("OPERATOR", "BRANCH_ADMIN", "ORG_ADMIN", "SUPER_ADMIN")

                        // Analytics reporting: BRANCH_ADMIN, ORG_ADMIN, SUPER_ADMIN
                        .requestMatchers("/api/v1/analytics/**").hasAnyRole("BRANCH_ADMIN", "ORG_ADMIN", "SUPER_ADMIN")

                        // Organization & branch administration
                        .requestMatchers(HttpMethod.POST, "/api/v1/orgs", "/api/v1/organizations").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/orgs/**", "/api/v1/organizations/**").hasAnyRole("ORG_ADMIN", "SUPER_ADMIN", "BRANCH_ADMIN", "OPERATOR")

                        // Branch counter management
                        .requestMatchers(HttpMethod.POST, "/api/v1/branches/*/counters").hasAnyRole("BRANCH_ADMIN", "ORG_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/branches/*/counters").hasAnyRole("OPERATOR", "BRANCH_ADMIN", "ORG_ADMIN", "SUPER_ADMIN")

                        // Authenticated user settings & preferences
                        .requestMatchers("/api/v1/users/**", "/api/v1/organization/preferences").authenticated()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(correlationIdFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "https://queue-less-five.vercel.app",
                "https://*.vercel.app",
                "http://localhost:3000",
                "http://localhost:5173",
                "*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "X-Tenant-ID", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
