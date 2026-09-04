package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.AccountQueryRequest;
import com.smarthis.operations.dto.response.AccountVo;

import java.time.LocalDate;

public interface AccountService {
    AccountVo generate(String cashierId, String cashierName, LocalDate accountDate);
    AccountVo submit(Long id);
    PageResult<AccountVo> query(AccountQueryRequest request);
    AccountVo receive(Long id, String receiverId);
}
