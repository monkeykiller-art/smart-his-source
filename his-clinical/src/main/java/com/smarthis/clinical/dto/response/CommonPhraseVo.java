package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class CommonPhraseVo {

    private Long id;

    private String phraseName;

    private String phraseContent;

    private String phraseType;

    private Long deptId;

    private Long userId;

    private Integer sortOrder;
}
