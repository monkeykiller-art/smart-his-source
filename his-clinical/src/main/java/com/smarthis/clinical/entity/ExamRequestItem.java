package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_exam_request_item")
public class ExamRequestItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long requestId;

    private Integer itemSeq;

    private String itemCode;

    private String itemName;

    private String itemType;

    private String spec;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private String bodyPart;

    private String methodDesc;

    private String itemStatus;

    private String confirmStatus;

    private LocalDateTime confirmTime;
}
