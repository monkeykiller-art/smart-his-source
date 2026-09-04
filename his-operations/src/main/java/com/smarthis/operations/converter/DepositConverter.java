package com.smarthis.operations.converter;

import com.smarthis.operations.dto.response.DepositAccountVo;
import com.smarthis.operations.dto.response.DepositVo;
import com.smarthis.operations.entity.Deposit;
import com.smarthis.operations.entity.DepositAccount;

public final class DepositConverter {

    private DepositConverter() {
    }

    public static DepositVo toVo(Deposit e) {
        DepositVo vo = new DepositVo();
        vo.setId(e.getId());
        vo.setDepositNo(e.getDepositNo());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setReceiptNo(e.getReceiptNo());
        vo.setAmount(e.getAmount());
        vo.setPayMethod(e.getPayMethod() != null ? e.getPayMethod().getValue() : null);
        vo.setDepositType(e.getDepositType() != null ? e.getDepositType().getValue() : null);
        vo.setBalanceBefore(e.getBalanceBefore());
        vo.setBalanceAfter(e.getBalanceAfter());
        vo.setCashierId(e.getCashierId());
        vo.setCashierName(e.getCashierName());
        vo.setChargeTime(e.getChargeTime());
        vo.setRemark(e.getRemark());
        vo.setDepositStatus(e.getDepositStatus());
        return vo;
    }

    public static DepositAccountVo toAccountVo(DepositAccount e) {
        DepositAccountVo vo = new DepositAccountVo();
        vo.setId(e.getId());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setTotalDeposit(e.getTotalDeposit());
        vo.setTotalCharged(e.getTotalCharged());
        vo.setBalance(e.getBalance());
        vo.setFrozenAmount(e.getFrozenAmount());
        vo.setAccountStatus(e.getAccountStatus());
        return vo;
    }
}
