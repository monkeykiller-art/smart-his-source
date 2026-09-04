package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FrequencyDictCreateRequest {

    @NotBlank(message = "freq code is required")
    private String freqCode;

    @NotBlank(message = "freq name is required")
    private String freqName;

    private String namePinyin;

    private Integer dailyTimes;

    private String freqDesc;

    private Integer sortOrder;

    private Integer dictStatus;
}
