package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVo {

    private Long id;

    private String orderNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String orderType;

    private String orderCategory;

    private String orderStatus;

    private Integer priority;

    private Integer isStat;

    private Integer isPrn;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime orderTime;

    private Long verifyNurseId;

    private LocalDateTime verifyTime;

    private Long cancelNurseId;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private Long executeDeptId;

    private String remark;

    private String groupNo;

    private Long billId;

    private LocalDateTime createdTime;

    private List<OrderItemVo> items;
}
