package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class FrequencyDictUpdateRequest {

    private String freqCode;

    private String freqName;

    private String namePinyin;

    private Integer dailyTimes;

    private String freqDesc;

    private Integer sortOrder;

    private Integer dictStatus;
}
