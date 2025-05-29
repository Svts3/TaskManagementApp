package com.example.taskmanagementapp.security;

import com.example.taskmanagementapp.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String jwt;
        String email;
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")){
            jwt = header.substring(7);
            if(jwtTokenProvider.validateToken(jwt)){
                email = jwtTokenProvider.extractEmailFromToken(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Extract permissions from token
                Map<Long, String[]> permissions = jwtTokenProvider.extractPermissionsFromToken(jwt);

                // Create an authentication with user details and permissions
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                // Add permissions as a detail to the authentication
                Map<String, Object> details = new HashMap<>();
                details.put("permissions", permissions);
                authentication.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
