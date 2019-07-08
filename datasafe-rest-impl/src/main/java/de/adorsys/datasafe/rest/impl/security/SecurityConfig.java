package de.adorsys.datasafe.rest.impl.security;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static de.adorsys.datasafe.rest.impl.security.SecurityConstants.TOKEN_HEADER;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] SWAGGER_RESOURCES = {
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(SWAGGER_RESOURCES).permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers(SecurityConstants.AUTH_LOGIN_URL).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), securityProperties))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(securityProperties.getDefaultUser())
                .password(passwordEncoder().encode(securityProperties.getDefaultPassword()))
                .authorities("ROLE_USER");
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

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    @SneakyThrows
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(authenticationManager(), securityProperties);
    }
}
