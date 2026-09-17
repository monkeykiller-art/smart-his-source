package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_saved_report_filter")
public class SavedReportFilter extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long userId;
    @NotBlank private String reportCode;
    @NotBlank private String filterName;
    @NotBlank private String configJson;
}
