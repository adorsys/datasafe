package de.adorsys.datasafe.rest.impl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.datasafe.rest.impl.security.SecurityConstants.TOKEN_HEADER;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc, AuthenticationManager authenticationManager) throws Exception {
        MvcRequestMatcher[] SWAGGER_RESOURCES = {
                mvc.pattern("/v2/api-docs"),
                mvc.pattern("/configuration/ui"),
                mvc.pattern("/swagger-resources"),
                mvc.pattern("/configuration/security"),
                mvc.pattern("/swagger-ui.html"),
                mvc.pattern("/webjars/**"),
                mvc.pattern("/swagger-resources/configuration/ui"),
                mvc.pattern("/swagger-ui.html")
        };

        http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(SWAGGER_RESOURCES).permitAll()
                        .requestMatchers(mvc.pattern("/static/**")).permitAll()
                        .requestMatchers(mvc.pattern(SecurityConstants.AUTH_LOGIN_URL)).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.OPTIONS, "/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthorizationFilter(authenticationManager, securityProperties))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Scope("prototype")
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username(securityProperties.getDefaultUser())
                .password(securityProperties.getDefaultPassword())
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
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder("bcrypt", encoders);
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
