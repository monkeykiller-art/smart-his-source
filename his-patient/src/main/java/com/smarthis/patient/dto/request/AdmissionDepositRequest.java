package com.smarthis.patient.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdmissionDepositRequest {

    private BigDecimal amount;

    private String payMethod;
}
