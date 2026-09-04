package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdrReportReviewRequest {

    @NotBlank(message = "reviewer id is required")
    private String reviewerId;

    private String reviewComment;

    @NotBlank(message = "report status is required")
    private String reportStatus;

    private Integer isReportedToAuthority;
}
