package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamRequestVo {

    private Long id;

    private String requestNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String requestType;

    private Integer isUrgent;

    private String clinicalDiagnosis;

    private String clinicalInfo;

    private Long requestDeptId;

    private Long executeDeptId;

    private String requestStatus;

    private LocalDateTime requestTime;

    private LocalDateTime resultTime;

    private String resultSummary;

    private String reportNo;

    private Integer isCritical;

    private Integer criticalAcknowledged;

    private Long criticalAckBy;

    private LocalDateTime criticalAckTime;

    private String reportUrl;

    private Integer isPrinted;

    private LocalDateTime printTime;

    private String remark;

    private LocalDateTime createdTime;

    private List<ExamRequestItemVo> items;
}
