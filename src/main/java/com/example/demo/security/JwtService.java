package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/** Issues and verifies signed JWTs; a token is an ID card whose signature proves it was issued here. */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        byte[] secretBytes = Decoders.BASE64.decode(secretBase64);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT signing key must contain at least 32 decoded bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenSeconds = accessTokenMinutes * 60;
        this.refreshTokenSeconds = refreshTokenDays * 24 * 60 * 60;
    }

    public String createAccessToken(UserDetails user) {
        return createToken(user, "access", accessTokenSeconds);
    }

    public String createRefreshToken(UserDetails user) {
        return createToken(user, "refresh", refreshTokenSeconds);
    }

    public boolean isValid(String token, UserDetails user, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(user.getUsername())
                    && expectedType.equals(claims.get("type", String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            // Invalid signatures, malformed values, and expired tokens are all rejected, never trusted.
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    private String createToken(UserDetails user, String type, long lifetimeSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", type) // Stops a longer-lived refresh token being used as an API access token.
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(lifetimeSeconds)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}