package com.smarthis.common.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthis.common.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.body() != null) {
            try (InputStream body = response.body().asInputStream()) {
                JsonNode node = objectMapper.readTree(body);
                JsonNode codeNode = node.get("code");
                JsonNode messageNode = node.get("message");
                if (codeNode != null && messageNode != null) {
                    int code = codeNode.asInt();
                    String message = messageNode.asText();
                    log.debug("Feign call {} returned business error: code={}, message={}",
                            methodKey, code, message);
                    return new BusinessException(code, message);
                }
            } catch (IOException e) {
                log.warn("Failed to parse Feign error response for {}", methodKey, e);
            }
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
