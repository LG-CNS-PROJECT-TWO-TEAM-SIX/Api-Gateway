    package com.team6.api_gateway.config;

    import com.team6.api_gateway.jwt.JwtTokenValidator;
    import com.team6.api_gateway.security.exception.RestAccessDeniedHandler;
    import com.team6.api_gateway.security.exception.RestAuthenticationEntryPoint;
    import com.team6.api_gateway.security.filter.JwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
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
    public class WebSecurityConfig {
        private final JwtTokenValidator jwtTokenValidator;
        private final RestAuthenticationEntryPoint authenticationEntryPoint;
        private final RestAccessDeniedHandler accessDeniedHandler;

        @Bean
        public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
            http
                    .cors(httpSecurityCorsConfigurer -> {
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
                    })
                    .csrf(AbstractHttpConfigurer::disable)
                    .securityMatcher("/**") // map current config to given resource path
                    .sessionManagement(sessionManagementConfigurer
                            -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .addFilterBefore(
                            new JwtAuthenticationFilter(jwtTokenValidator),
                            UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling((exceptionConfig) ->
                            exceptionConfig
                                    .authenticationEntryPoint(authenticationEntryPoint)
                                    .accessDeniedHandler(accessDeniedHandler))
                    .authorizeHttpRequests(registry -> registry
                            .requestMatchers("/api/user/v1/auth/**").permitAll()
                            .anyRequest().authenticated()
                    );
            return http.build();
        }
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
    // config.setAllowedOrigins(List.of("*"));
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));config.setAllowedHeaders(List.of("*"));
            config.setExposedHeaders(List.of("*"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }
    }