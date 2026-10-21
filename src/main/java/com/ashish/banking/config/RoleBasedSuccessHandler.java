package com.ashish.banking.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication)
                        throws IOException, ServletException {

                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                System.out.println("Logged-in authorities: " + authentication.getAuthorities());
                if (isAdmin) {
                        response.sendRedirect("/admin/dashboard");
                        System.out.println("Admin redirecting to: /admin/dashboard");
                } else {
                        response.sendRedirect("/");
                }
        }
}