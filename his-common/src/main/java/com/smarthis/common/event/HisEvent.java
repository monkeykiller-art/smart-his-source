package com.smarthis.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HisEvent<T> implements Serializable {
    private String eventId;
    private String eventType;
    private String source;
    private String traceId;
    private LocalDateTime occurredAt;
    private String orgCode;
    private T payload;

    public static <T> HisEvent<T> of(String eventType, String source, T payload) {
        HisEvent<T> event = new HisEvent<>();
        event.setEventId(java.util.UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setSource(source);
        event.setOccurredAt(LocalDateTime.now());
        event.setPayload(payload);
        return event;
    }
}
