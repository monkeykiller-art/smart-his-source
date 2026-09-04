package com.smarthis.patient.dto.response;

import lombok.Data;

@Data
public class DepartmentVo {

    private Long id;

    private String deptCode;

    private String deptName;

    private String deptType;

    private Long parentId;

    private Integer sortOrder;

    private String deptStatus;

    private String description;
}
