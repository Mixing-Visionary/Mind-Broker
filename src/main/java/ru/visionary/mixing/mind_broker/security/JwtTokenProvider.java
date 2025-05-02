package ru.visionary.mixing.mind_broker.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.config.properties.JwtProperties;
import ru.visionary.mixing.mind_broker.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.email())
                .claim("roles", user.admin() ? "ROLE_ADMIN" : "ROLE_USER")
                .issuedAt(new Date())
                .expiration(getExpiration(jwtProperties.accessTokenExpiration()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.email())
                .issuedAt(new Date())
                .expiration(getExpiration(jwtProperties.refreshTokenExpiration()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Date getExpiration(Duration expiration) {
        return new Date(System.currentTimeMillis() + expiration.toMillis());
    }

    public boolean validateToken(String token) {
        try {
            getParser().build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return getParser().build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private JwtParserBuilder getParser() {
        return Jwts.parser().verifyWith(secretKey);
    }
}
