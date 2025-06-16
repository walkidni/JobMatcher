package com.walid.jobmatcher.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("=== JWT Filter Processing ===");
        logger.info("Request URI: {}", requestURI);
        logger.info("Request Method: {}", request.getMethod());

        // Skip JWT processing for public endpoints
        if (requestURI.startsWith("/auth/")) {
            logger.info("Skipping JWT processing for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No valid Authorization header found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            logger.info("Extracted token: {}", token);
            
            if (!jwtUtil.isTokenValid(token)) {
                logger.warn("Token validation failed");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return;
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            logger.info("Extracted username: {} and role: {}", username, role);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Loading user details for username: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("Loaded user details: {}", userDetails);
                logger.info("User authorities: {}", userDetails.getAuthorities());

                // Create authentication token with role from JWT
                String roleWithPrefix = "ROLE_" + role;
                logger.info("Creating authentication token with role: {}", roleWithPrefix);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentication set in SecurityContext for user: {} with role: {}", username, roleWithPrefix);
                logger.info("Current SecurityContext authorities: {}", 
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            } else {
                logger.warn("Username is null or authentication already exists in SecurityContext");
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error processing token: " + e.getMessage());
            return;
        }

        logger.info("=== JWT Filter Processing Complete ===");
        filterChain.doFilter(request, response);
    }
}

