package com.ashish.banking.config;

import com.ashish.banking.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider(
                        CustomUserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        AuthenticationProvider authenticationProvider,
                        RoleBasedSuccessHandler successHandler) throws Exception {

                http
                                .authenticationProvider(authenticationProvider)

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/login",
                                                                "/register",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/create-account",
                                                                "/check-balance",
                                                                "/deposit",
                                                                "/withdraw",
                                                                "/transfer",
                                                                "/transactions",
                                                                "/customer/**")
                                                .hasRole("CUSTOMER")

                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .successHandler(successHandler)
                                                .failureUrl("/login?error")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation(fixation -> fixation.changeSessionId()))

                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedHandler((request, response, exception) -> {
                                                        response.sendError(
                                                                        HttpStatus.FORBIDDEN.value(),
                                                                        "You are not authorized to access this resource.");
                                                }));

                return http.build();
        }

        @Bean
        public HttpSessionEventPublisher httpSessionEventPublisher() {
                return new HttpSessionEventPublisher();
        }
}