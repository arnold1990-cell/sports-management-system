package com.sportsms.auth;

import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsNullLikeBearerToken() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/me");
        request.addHeader("Authorization", "Bearer null");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(jwtService, never()).parseToken("null");
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void setsAuthenticationWithRolePrefixWhenTokenIsValid() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/me");
        request.addHeader("Authorization", "Bearer valid-token");

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject("user@example.com");
        claims.put("roles", List.of("ADMIN", "ROLE_MANAGER"));
        when(jwtService.parseToken("valid-token")).thenReturn(claims);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertNotNull(authentication);
        Assertions.assertEquals("user@example.com", authentication.getName());
        Assertions.assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        Assertions.assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }
}
