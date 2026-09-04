package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.AccountCreateRequest;
import com.smarthis.operations.dto.request.AccountQueryRequest;
import com.smarthis.operations.dto.response.AccountVo;

public interface AccountService {
    AccountVo create(AccountCreateRequest request);
    AccountVo getById(Long id);
    PageResult<AccountVo> query(AccountQueryRequest request);
    AccountVo receive(Long id, String receiverId);
}
