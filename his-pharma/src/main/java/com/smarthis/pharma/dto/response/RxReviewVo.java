package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RxReviewVo {

    private Long id;

    private String reviewNo;

    private Long orderId;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long doctorId;

    private Long deptId;

    private String prescriptionType;

    private String reviewStatus;

    private String reviewResult;

    private String reviewerId;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private String rejectReason;

    private Integer warningCount;

    private Integer errorCount;

    private String remark;

    private List<RxReviewItemVo> items;
}
