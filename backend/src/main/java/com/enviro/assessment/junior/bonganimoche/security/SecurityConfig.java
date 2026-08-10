package com.enviro.assessment.junior.bonganimoche.config;

import com.enviro.assessment.junior.bonganimoche.security.JwtAuthenticationEntryPoint;
import com.enviro.assessment.junior.bonganimoche.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the API.
 *
 * Uses the component-based SecurityFilterChain style rather than the deprecated
 * WebSecurityConfigurerAdapter, which was removed in Spring Security 6.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * BCrypt: deliberately slow and salted per-hash, so identical passwords
     * produce different hashes and brute-forcing is expensive. A general-purpose
     * digest such as SHA-256 is fast by design, which is exactly the wrong
     * property for password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager so AuthServiceImpl can delegate
     * credential checking rather than comparing hashes by hand.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protects cookie-based sessions, where the browser attaches
            // credentials automatically. This API is stateless and authenticates
            // via an Authorization header, which a cross-site form post cannot
            // set, so the protection has nothing to guard.
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // No server-side session. Every request carries its own token, which
            // is what allows the API to scale horizontally without sticky
            // sessions or a shared session store.
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Replaces Spring Security's default empty 403 with a structured
            // 401 carrying the standard ErrorResponse shape.
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

            .authorizeHttpRequests(auth -> auth
                    // Login must be reachable without a token.
                    .requestMatchers("/auth/**").permitAll()
                    // Development convenience; would not be exposed in production.
                    .requestMatchers("/h2-console/**").permitAll()
                    // Browsers send an unauthenticated OPTIONS preflight before
                    // any cross-origin request carrying an Authorization header.
                    // Blocking it would break every call from the React app.
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Everything else requires authentication. Default-deny:
                    // a new endpoint is protected unless explicitly opened.
                    .anyRequest().authenticated())

            // The H2 console renders inside a frame; the default DENY blocks it.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // Runs before the username/password filter so a valid bearer token
            // authenticates the request before form login is ever considered.
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS for the React dev server.
     *
     * Configured here rather than with @CrossOrigin on controllers so the policy
     * lives in one place and applies uniformly — including to the preflight
     * requests that never reach a controller.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Without this the browser hides the header from JavaScript, and the
        // CSV download cannot read the server-supplied filename.
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}