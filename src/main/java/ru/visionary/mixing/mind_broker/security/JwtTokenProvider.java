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
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", user.getAdmin() ? "ROLE_ADMIN" : "ROLE_USER")
                .issuedAt(new Date())
                .expiration(parseExpiration(jwtProperties.getAccessTokenExpiration()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(parseExpiration(jwtProperties.getRefreshTokenExpiration()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Date parseExpiration(String expiration) {
        return new Date(System.currentTimeMillis() +
                Duration.parse(expiration).toMillis());
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
