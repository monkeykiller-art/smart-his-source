package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class FrequencyDictVo {

    private Long id;

    private String freqCode;

    private String freqName;

    private String namePinyin;

    private Integer dailyTimes;

    private String freqDesc;

    private Integer sortOrder;

    private Integer dictStatus;
}
