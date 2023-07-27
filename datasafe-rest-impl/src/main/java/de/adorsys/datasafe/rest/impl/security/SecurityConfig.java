package de.adorsys.datasafe.rest.impl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static de.adorsys.datasafe.rest.impl.security.SecurityConstants.TOKEN_HEADER;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private static final String[] SWAGGER_RESOURCES = {
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/configuration/ui",
            "/swagger-ui.html"
    };

    private final SecurityProperties securityProperties;

    SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(SWAGGER_RESOURCES).permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers(SecurityConstants.AUTH_LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthorizationFilter(authenticationManager, securityProperties))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username(securityProperties.getDefaultUser())
                .password(passwordEncoder.encode(securityProperties.getDefaultPassword()))
                .authorities("ROLE_USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration authConfig = new CorsConfiguration().applyPermitDefaultValues();
        authConfig.addExposedHeader(TOKEN_HEADER);
        source.registerCorsConfiguration(SecurityConstants.AUTH_LOGIN_URL, authConfig);

        CorsConfiguration globalConfig = new CorsConfiguration().applyPermitDefaultValues();
        globalConfig.addAllowedMethod(HttpMethod.OPTIONS);
        globalConfig.addAllowedMethod(HttpMethod.PUT);
        globalConfig.addAllowedMethod(HttpMethod.DELETE);
        source.registerCorsConfiguration("/**", globalConfig);

        return source;
    }
}
