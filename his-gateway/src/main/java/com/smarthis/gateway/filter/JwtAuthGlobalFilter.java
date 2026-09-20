package com.smarthis.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthis.gateway.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final SecretKey key;
    private final SecurityProperties securityProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(
            @Value("${his.jwt.secret}") String secret,
            SecurityProperties securityProperties,
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.securityProperties = securityProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhitelisted(path)) {
            ServerHttpRequest cleaned = stripUserHeaders(exchange.getRequest());
            return chain.filter(exchange.mutate().request(cleaned).build());
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, 5004, "missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, 5003, "token expired");
        } catch (Exception e) {
            return unauthorized(exchange, 5004, "invalid token");
        }

        if ("refresh".equals(claims.get("type", String.class))) {
            return unauthorized(exchange, 5004, "refresh token cannot be used for API access");
        }

        String jti = claims.getId();
        return redisTemplate.hasKey("his:auth:blacklist:" + jti)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, 5004, "token has been revoked");
                    }

                    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");

                    ServerHttpRequest.Builder requestBuilder = stripUserHeaders(exchange.getRequest()).mutate()
                            .header("X-User-Id", claims.getSubject())
                            .header("X-Username", claims.get("un", String.class))
                            .header("X-Real-Name", claims.get("rn", String.class))
                            .header("X-Roles", claims.get("roles", String.class))
                            .header("X-Permissions", claims.get("perms", String.class) != null ? claims.get("perms", String.class) : "")
                            .header("X-Trace-Id", traceId != null ? traceId : "");

                    Object deptId = claims.get("dept");
                    if (deptId != null) {
                        requestBuilder.header("X-Dept-Id", deptId.toString());
                    }

                    ServerHttpRequest request = requestBuilder.build();

                    return chain.filter(exchange.mutate().request(request).build());
                });
    }

    private boolean isWhitelisted(String path) {
        for (String pattern : securityProperties.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private ServerHttpRequest stripUserHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-Username");
                    headers.remove("X-Real-Name");
                    headers.remove("X-Dept-Id");
                    headers.remove("X-Roles");
                    headers.remove("X-Permissions");
                })
                .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, int code, String message) {
        log.warn("Auth rejected [{}] {} — {}", code, message, exchange.getRequest().getURI());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] fallback = ("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
