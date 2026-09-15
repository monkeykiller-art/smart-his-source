package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillRegistrationRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    @NotNull(message = "挂号编号不能为空")
    @Positive(message = "挂号编号必须大于零")
    private Long regId;

    private String regNo;

    @NotNull(message = "挂号金额不能为空")
    @DecimalMin(value = "0", message = "挂号金额不能为负数")
    @Digits(integer = 14, fraction = 4, message = "挂号金额最多支持四位小数")
    private BigDecimal amount;

    private Long encounterId;

    private String visitType;

    private Long deptId;

    private String billType;

    private String remark;
}
