package com.smarthis.operations.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountQueryRequest extends PageQuery {

    private String cashierId;

    private String accountStatus;

    private LocalDate accountDateFrom;

    private LocalDate accountDateTo;
}
