package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FrequencyDictQueryRequest extends PageQuery {

    private String keyword;

    private Integer dailyTimes;

    private Integer dictStatus;
}
