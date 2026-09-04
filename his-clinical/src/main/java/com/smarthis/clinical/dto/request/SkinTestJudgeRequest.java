package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkinTestJudgeRequest {

    @NotBlank(message = "test result is required")
    private String testResult;

    private Long judgeNurseId;
}
