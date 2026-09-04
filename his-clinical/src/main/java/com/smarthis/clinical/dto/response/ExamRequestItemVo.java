package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRequestItemVo {

    private Long id;

    private Long requestId;

    private Integer itemSeq;

    private String itemCode;

    private String itemName;

    private String itemType;

    private String spec;

    private java.math.BigDecimal quantity;

    private java.math.BigDecimal unitPrice;

    private java.math.BigDecimal amount;

    private String bodyPart;

    private String methodDesc;

    private String itemStatus;

    private String confirmStatus;

    private LocalDateTime confirmTime;
}
