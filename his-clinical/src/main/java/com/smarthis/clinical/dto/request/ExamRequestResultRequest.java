package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class ExamRequestResultRequest {

    private String resultSummary;

    private String reportNo;

    private Integer isCritical;

    private String reportUrl;
}
