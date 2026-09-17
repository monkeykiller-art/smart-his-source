package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_rx_review_item")
public class RxReviewItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long reviewId;

    private Long orderItemId;

    private String alertType;

    private String alertLevel;

    private String drugCodeA;

    private String drugNameA;

    private String drugCodeB;

    private String drugNameB;

    private String alertDesc;

    private String suggestion;

    private Integer isOverridden;

    private String overrideReason;

    private String overrideBy;
}
