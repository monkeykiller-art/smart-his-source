package com.smarthis.pharma.dto.request;

import lombok.Data;

@Data
public class RxReviewItemRequest {

    private Long orderItemId;

    private String alertType;

    private String alertLevel;

    private String drugCodeA;

    private String drugNameA;

    private String drugCodeB;

    private String drugNameB;

    private String alertDesc;

    private String suggestion;
}
