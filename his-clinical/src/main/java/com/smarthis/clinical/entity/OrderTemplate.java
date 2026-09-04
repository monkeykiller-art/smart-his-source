package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_order_template")
public class OrderTemplate extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String templateName;

    private String templateLevel;

    private Long deptId;

    private String templateCategory;

    private String levelType;

    private String orderType;

    private Integer sortOrder;

    private Integer templateStatus;
}
