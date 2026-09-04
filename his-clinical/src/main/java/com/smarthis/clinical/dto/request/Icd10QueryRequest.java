package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Icd10QueryRequest extends PageQuery {

    private String keyword;

    private String chapter;

    private String category;

    private Integer dictStatus;
}
