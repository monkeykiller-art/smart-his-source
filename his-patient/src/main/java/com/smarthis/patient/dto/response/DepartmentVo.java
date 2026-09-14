package com.smarthis.patient.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class DepartmentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String deptCode;

    private String deptName;

    private String deptType;

    private Long parentId;

    private Integer sortOrder;

    private String deptStatus;

    private String description;
}
