package com.team1.mixIt.common.config;

import com.team1.mixIt.common.filter.JwtAuthenticationFilter;
import com.team1.mixIt.common.filter.LoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter authenticationFilter;
    private static final String[] AUTH_WHITELIST = {
            "/api/v1/login",
            "/api/v1/accounts/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/tags/popular",
            "/api/v1/home/**"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.PUT, "/api/v1/accounts/password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/images").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/terms").permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new LoggingFilter(), JwtAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider)
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // 모든 origin 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 모든 method 허용
        config.setAllowedHeaders(List.of("*")); // 모든 header 허용
        config.setAllowCredentials(true); // 인증 정보 허용 (옵션)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 설정 적용
        return source;
    }
}
