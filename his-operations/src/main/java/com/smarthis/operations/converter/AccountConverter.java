package com.smarthis.operations.converter;

import com.smarthis.operations.dto.response.AccountVo;
import com.smarthis.operations.entity.Account;

public final class AccountConverter {

    private AccountConverter() {
    }

    public static AccountVo toVo(Account e) {
        AccountVo vo = new AccountVo();
        vo.setId(e.getId());
        vo.setAccountNo(e.getAccountNo());
        vo.setCashierId(e.getCashierId());
        vo.setCashierName(e.getCashierName());
        vo.setSettleType(e.getSettleType());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setCashAmount(e.getCashAmount());
        vo.setPosAmount(e.getPosAmount());
        vo.setOtherAmount(e.getOtherAmount());
        vo.setBillCount(e.getBillCount());
        vo.setAccountDate(e.getAccountDate());
        vo.setSubmitTime(e.getSubmitTime());
        vo.setReceiveTime(e.getReceiveTime());
        vo.setReceiverId(e.getReceiverId());
        vo.setAccountStatus(e.getAccountStatus() != null ? e.getAccountStatus().getValue() : null);
        vo.setReceiveStatus(e.getReceiveStatus());
        vo.setPrintCount(e.getPrintCount());
        vo.setRemark(e.getRemark());
        return vo;
    }
}
