package com.smarthis.auth.support;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtProvider(
            @Value("${his.jwt.secret}") String secret,
            @Value("${his.jwt.access-token-ttl-minutes}") int accessMinutes,
            @Value("${his.jwt.refresh-token-ttl-days}") int refreshDays) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenTtlMs = accessMinutes * 60_000L;
        this.refreshTokenTtlMs = refreshDays * 86_400_000L;
    }

    public String createAccessToken(Long userId, String username, String realName,
                                    Long deptId, String roles, String permissions) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenTtlMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("un", username)
                .claim("rn", realName)
                .claim("dept", deptId)
                .claim("roles", roles)
                .claim("perms", permissions)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenTtlMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getJti(String token) {
        return parseToken(token).getId();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlMs / 1000;
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlMs / 1000;
    }
}
