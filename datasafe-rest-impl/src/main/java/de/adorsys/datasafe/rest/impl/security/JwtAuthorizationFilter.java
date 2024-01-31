package de.adorsys.datasafe.rest.impl.security;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final SecurityProperties securityProperties;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
                                  SecurityProperties securityProperties) {
        super(authenticationManager);
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        String header = request.getHeader(SecurityConstants.TOKEN_HEADER);

        if (Strings.isNullOrEmpty(header) || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (Strings.isNullOrEmpty(token)) {
            return null;
        }

        try {
            return tryAuthenticate(token);
        } catch (ExpiredJwtException exception) {
            log.warn("Request to parse expired JWT : {} failed", token, exception);
        } catch (UnsupportedJwtException exception) {
            log.warn("Request to parse unsupported JWT : {} failed", token, exception);
        } catch (MalformedJwtException exception) {
            log.warn("Request to parse invalid JWT : {} failed", token, exception);
        } catch (SignatureException exception) {
            log.warn("Request to parse JWT with invalid signature : {} failed", token, exception);
        } catch (IllegalArgumentException exception) {
            log.warn("Request to parse empty or null JWT : {} failed", token, exception);
        }

        return null;
    }

    private UsernamePasswordAuthenticationToken tryAuthenticate(String token) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        SecretKey secret = Keys.hmacShaKeyFor(signingKey);

        Jws<Claims> parsedToken = Jwts.parser()
                .verifyWith(secret).build()
                .parseSignedClaims(token.replace(SecurityConstants.TOKEN_PREFIX, ""));

        String username = parsedToken
                .getPayload()
                .getSubject();

        List<SimpleGrantedAuthority> authorities = ((List<?>) parsedToken.getPayload()
                .get(SecurityConstants.ROLES_NAME)).stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .toList();

        if (!Strings.isNullOrEmpty(username)) {
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }

        return null;
    }
}
