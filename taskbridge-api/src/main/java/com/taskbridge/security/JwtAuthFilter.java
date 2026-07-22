package com.taskbridge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Extracts and validates the JWT from the Authorization header.
 * On success: populates {@link TenantContext} and Spring SecurityContext.
 * On failure: clears context and lets the security chain return 401.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith(BEARER_PREFIX)) {
                chain.doFilter(request, response);
                return;
            }

            String token = header.substring(BEARER_PREFIX.length());

            Claims claims  = jwtService.validateAndExtract(token);
            String subject = jwtService.extractSubject(claims);
            UUID tenantId  = jwtService.extractTenantId(claims);

            TenantContext.set(tenantId, subject);

            MDC.put("tenantId", tenantId.toString());
            MDC.put("userId",   subject);

            var auth = new UsernamePasswordAuthenticationToken(
                subject, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            TenantContext.clear();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token");
        } finally {
            TenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("userId");
        }
    }
}

