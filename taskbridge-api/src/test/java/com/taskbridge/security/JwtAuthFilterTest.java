package com.taskbridge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void should_returnUnauthorizedAndNotContinueChain_when_jwtIsInvalid() throws ServletException, IOException {
        JwtService jwtService = mock(JwtService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateAndExtract("invalid-token"))
            .thenThrow(new JwtException("bad token"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void should_continueChain_when_jwtIsValid() throws ServletException, IOException {
        JwtService jwtService = mock(JwtService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtService);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        UUID tenantId = UUID.randomUUID();

        when(jwtService.validateAndExtract("valid-token")).thenReturn(claims);
        when(jwtService.extractSubject(claims)).thenReturn("user@example.com");
        when(jwtService.extractTenantId(claims)).thenReturn(tenantId);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
    }
}

