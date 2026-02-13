package com.sportsms.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String headerPreview = previewAuthorizationHeader(header);
        log.debug("JWT filter request path={}, hasAuthorizationHeader={}, authorizationPreview={}",
                request.getRequestURI(), header != null && !header.isBlank(), headerPreview);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (token.isBlank() || "null".equalsIgnoreCase(token) || "undefined".equalsIgnoreCase(token)) {
            log.debug("JWT filter skipped authentication for path={} because bearer token is blank/null-like", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseToken(token);
            String subject = claims.getSubject();
            Object roleClaims = claims.get("roles");
            var authorities = extractRoles(roleClaims)
                    .filter(role -> !role.isBlank())
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            if (subject != null && !subject.isBlank()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        subject, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT filter set authentication for subject={} path={}", subject, request.getRequestURI());
            } else {
                log.debug("JWT filter skipped authentication for path={} because subject is blank", request.getRequestURI());
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.debug("JWT parsing failed for path={}: {}", request.getRequestURI(), ex.getMessage());
        }

        log.debug("JWT filter securityContextAuthenticated={}",
                SecurityContextHolder.getContext().getAuthentication() != null
                        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        filterChain.doFilter(request, response);
    }

    private Stream<String> extractRoles(Object roleClaims) {
        if (roleClaims instanceof Collection<?> collection) {
            return collection.stream().filter(Objects::nonNull).map(Object::toString);
        }
        if (roleClaims instanceof String role) {
            return Stream.of(role);
        }
        return Stream.empty();
    }

    private String previewAuthorizationHeader(String header) {
        if (header == null || header.isBlank()) {
            return "<none>";
        }
        int previewLength = Math.min(15, header.length());
        return header.substring(0, previewLength);
    }
}
