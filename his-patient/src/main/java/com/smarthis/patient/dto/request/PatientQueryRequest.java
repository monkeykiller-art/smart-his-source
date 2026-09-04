package com.smarthis.patient.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PatientQueryRequest extends PageQuery {

    private String keyword;

    private String idType;

    private String idNo;

    private String phone;
}
