package com.smarthis.pharma.dto.response;

import lombok.Data;

@Data
public class RxReviewItemVo {

    private Long id;

    private Long reviewId;

    private Long orderItemId;

    private String alertType;

    private String alertLevel;

    private String drugCodeA;

    private String drugNameA;

    private String drugCodeB;

    private String drugNameB;

    private String alertDesc;

    private String suggestion;

    private Integer isOverridden;

    private String overrideReason;

    private String overrideBy;
}
