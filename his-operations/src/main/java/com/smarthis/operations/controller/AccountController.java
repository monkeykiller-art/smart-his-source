package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.AccountQueryRequest;
import com.smarthis.operations.dto.response.AccountVo;
import com.smarthis.operations.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/operations/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/generate")
    public ApiResponse<AccountVo> generate(
            @RequestParam String cashierId,
            @RequestParam String cashierName,
            @RequestParam LocalDate accountDate) {
        return ApiResponse.ok(accountService.generate(cashierId, cashierName, accountDate));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<AccountVo> submit(@PathVariable Long id) {
        return ApiResponse.ok(accountService.submit(id));
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<AccountVo> receive(@PathVariable Long id, @RequestParam String receiverId) {
        return ApiResponse.ok(accountService.receive(id, receiverId));
    }

    @GetMapping
    public ApiResponse<PageResult<AccountVo>> query(AccountQueryRequest request) {
        return ApiResponse.ok(accountService.query(request));
    }
}
