package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RxReviewApproveRequest {

    @NotBlank(message = "reviewer id is required")
    private String reviewerId;

    private String reviewerName;

    private String reviewResult;
}
