package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommonPhraseUpdateRequest {

    @NotBlank(message = "phrase name is required")
    private String phraseName;

    @NotBlank(message = "phrase content is required")
    private String phraseContent;

    @NotNull(message = "phrase type is required")
    private String phraseType;

    private Long deptId;

    private Long userId;

    private Integer sortOrder;
}
