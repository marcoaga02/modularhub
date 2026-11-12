package com.marcoaga02.modularhub.fixture;

import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class SecurityFixtures {

    private SecurityFixtures() {
    }

    public static Jwt jwt(String subject) {
        return jwt(subject, Map.of());
    }

    public static Jwt jwt(String subject, Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    public static List<GrantedAuthority> roles(String... roles) {
        return Arrays.stream(roles)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role))
                .toList();
    }

    public static Jwt authenticate(Jwt jwt, List<GrantedAuthority> authorities) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, authorities));
        return jwt;
    }

    public static Jwt authenticate(String subject, String... roles) {
        return authenticate(jwt(subject), roles(roles));
    }
}
