package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RxReviewRejectRequest {

    @NotBlank(message = "reviewer id is required")
    private String reviewerId;

    private String reviewerName;

    @NotBlank(message = "reject reason is required")
    private String rejectReason;
}
