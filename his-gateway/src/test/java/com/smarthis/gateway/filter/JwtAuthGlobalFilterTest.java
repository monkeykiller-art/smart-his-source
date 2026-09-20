package com.smarthis.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthis.gateway.config.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthGlobalFilterTest {

    @Test
    void omitsDepartmentHeaderWhenTokenHasNoDepartment() {
        String secret = Encoders.BASE64.encode(new byte[64]);
        SecretKey key = Keys.hmacShaKeyFor(new byte[64]);
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));

        JwtAuthGlobalFilter filter = new JwtAuthGlobalFilter(secret, new SecurityProperties(), redisTemplate, new ObjectMapper());
        String token = Jwts.builder()
                .subject("1001")
                .claim("un", "admin")
                .claim("rn", "System Admin")
                .claim("roles", "ADMIN")
                .id("test-jti")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        GatewayFilterChain chain = current -> {
            forwarded.set(current);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(forwarded.get()).isNotNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("1001");
        assertThat(forwarded.get().getRequest().getHeaders().containsKey("X-Dept-Id")).isFalse();
    }
}
