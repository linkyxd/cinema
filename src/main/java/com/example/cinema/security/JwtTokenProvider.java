package com.example.cinema.security;

import com.example.cinema.model.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SESSION_ID = "sessionId";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;

    public long getRefreshExpiryMs() { return refreshExpiryMs; }

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiry-ms}") long accessExpiryMs,
            @Value("${app.jwt.refresh-expiry-ms}") long refreshExpiryMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    public String generateAccessToken(UserAccount user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + accessExpiryMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UserAccount user, String sessionId) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + refreshExpiryMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        return validateToken(token, TYPE_ACCESS);
    }

    public Claims validateRefreshToken(String token) {
        return validateToken(token, TYPE_REFRESH);
    }

    private Claims validateToken(String token, String expectedType) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            String type = claims.get(CLAIM_TYPE, String.class);
            if (type == null || !type.equals(expectedType)) {
                throw new JwtException("Invalid token type");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired");
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Invalid token: " + e.getMessage());
        }
    }
}
