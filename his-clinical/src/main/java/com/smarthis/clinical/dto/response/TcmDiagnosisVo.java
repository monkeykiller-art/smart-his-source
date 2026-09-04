package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class TcmDiagnosisVo {

    private Long id;

    private String tcmCode;

    private String tcmName;

    private String namePinyin;

    private String syndromeCode;

    private String syndromeName;

    private String category;

    private Integer sortOrder;

    private Integer dictStatus;
}
