package com.smarthis.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        int code;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            code = toBusinessCode(status.value());
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else {
            log.error("Unhandled gateway exception", ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = 5000;
            message = "gateway internal error";
        }

        if (status.value() >= 500) {
            log.error("Gateway error [{}] {} — {}", status.value(), message, exchange.getRequest().getURI(), ex);
        } else {
            log.warn("Gateway error [{}] {} — {}", status.value(), message, exchange.getRequest().getURI());
        }

        response.setStatusCode(status);
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

    private static int toBusinessCode(int httpStatus) {
        return switch (httpStatus) {
            case 400 -> 4000;
            case 401 -> 4001;
            case 403 -> 4003;
            case 404 -> 4004;
            case 405 -> 4005;
            case 502 -> 5002;
            case 503 -> 5001;
            default -> httpStatus;
        };
    }
}
