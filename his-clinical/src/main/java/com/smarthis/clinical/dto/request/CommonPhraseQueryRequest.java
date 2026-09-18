package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class CommonPhraseQueryRequest {

    private String phraseType;

    private Long deptId;

    private Long userId;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
