package com.re.examholiday.config;

import com.re.examholiday.security.CustomAccessDeniedHandler;
import com.re.examholiday.security.JwtAuthenticationEntryPoint;
import com.re.examholiday.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (stateless API using JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Authentication & MVC Views
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/dashboard",
                                "/profile",
                                "/change-password",
                                "/accounts/**",
                                "/employees/**",
                                "/customers/**",
                                "/customer/reservations",
                                "/customer/menu",
                                "/tables/**",
                                "/reservations/**",
                                "/menu/**",
                                "/categories/**",
                                "/orders/**",
                                "/kitchen/**",
                                "/payments/**",
                                "/promotions/**",
                                "/reports/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/otp/request",
                                "/api/auth/otp/verify",
                                "/api/auth/refresh"
                        ).permitAll()

                        // Admin only
                        .requestMatchers("/api/users/manage/**").hasRole("ADMIN")

                        // Admin & Manager
                        .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN", "MANAGER")

                        // Public REST GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/menu", "/api/customer/menu/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/promotions", "/api/customer/promotions/**").permitAll()

                        // Allow GET requests for lists to all authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/kitchen/**").authenticated()

                        // Admin & Manager for modifying
                        .requestMatchers("/api/menu-items/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/promotions/**").hasRole("MANAGER")

                        // Cashier & Admin for modifying
                        .requestMatchers("/api/payments/**").hasAnyRole("CASHIER", "ADMIN")

                        // Waitstaff & Customer - Orders modifying
                        .requestMatchers("/api/orders/**").hasAnyRole("WAITSTAFF", "CUSTOMER", "ADMIN", "MANAGER")

                        // Chef - Kitchen modifying
                        .requestMatchers("/api/kitchen/**").hasAnyRole("CHEF", "ADMIN")

                        // All authenticated users
                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
