package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.operations.converter.AccountConverter;
import com.smarthis.operations.dto.request.AccountQueryRequest;
import com.smarthis.operations.dto.response.AccountVo;
import com.smarthis.operations.entity.Account;
import com.smarthis.operations.entity.Settlement;
import com.smarthis.operations.enums.AccountStatus;
import com.smarthis.operations.enums.PayMethod;
import com.smarthis.operations.mapper.AccountMapper;
import com.smarthis.operations.mapper.SettlementMapper;
import com.smarthis.operations.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final SettlementMapper settlementMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public AccountVo generate(String cashierId, String cashierName, LocalDate accountDate) {
        LambdaQueryWrapper<Settlement> query = new LambdaQueryWrapper<>();
        query.eq(Settlement::getCashierId, cashierId)
                .eq(Settlement::getSettleStatus, "SETTLED")
                .apply("DATE(settle_time) = {0}", accountDate);
        List<Settlement> settlements = settlementMapper.selectList(query);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal posAmount = BigDecimal.ZERO;
        BigDecimal otherAmount = BigDecimal.ZERO;

        for (Settlement s : settlements) {
            totalAmount = totalAmount.add(s.getSelfPayAmount());
            if (s.getPayMethod() == PayMethod.CASH) {
                cashAmount = cashAmount.add(s.getSelfPayAmount());
            } else if (s.getPayMethod() == PayMethod.POS) {
                posAmount = posAmount.add(s.getSelfPayAmount());
            } else {
                otherAmount = otherAmount.add(s.getSelfPayAmount());
            }
        }

        Account account = new Account();
        account.setAccountNo(bizNoGenerator.next(BizNoType.ACCOUNT));
        account.setCashierId(cashierId);
        account.setCashierName(cashierName);
        account.setSettleType("FINAL");
        account.setTotalAmount(totalAmount);
        account.setCashAmount(cashAmount);
        account.setPosAmount(posAmount);
        account.setOtherAmount(otherAmount);
        account.setBillCount(settlements.size());
        account.setAccountDate(accountDate);
        account.setAccountStatus(AccountStatus.PENDING);
        account.setReceiveStatus("UNRECEIVED");
        account.setPrintCount(0);
        accountMapper.insert(account);

        log.info("Account generated: accountNo={}, cashierId={}, totalAmount={}, billCount={}",
                account.getAccountNo(), cashierId, totalAmount, settlements.size());
        return AccountConverter.toVo(account);
    }

    @Override
    @Transactional
    public AccountVo submit(Long accountId) {
        Account account = accountMapper.selectById(accountId);
        if (account == null || account.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        }
        if (account.getAccountStatus() != AccountStatus.PENDING) {
            throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }

        account.setAccountStatus(AccountStatus.SUBMITTED);
        account.setSubmitTime(LocalDateTime.now());
        accountMapper.updateById(account);

        log.info("Account submitted: accountNo={}", account.getAccountNo());
        return AccountConverter.toVo(account);
    }

    @Override
    @Transactional
    public AccountVo receive(Long accountId, String receiverId) {
        Account account = accountMapper.selectById(accountId);
        if (account == null || account.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        }
        if (account.getAccountStatus() != AccountStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }

        account.setAccountStatus(AccountStatus.RECEIVED);
        account.setReceiveTime(LocalDateTime.now());
        account.setReceiverId(receiverId);
        account.setReceiveStatus("RECEIVED");
        accountMapper.updateById(account);

        log.info("Account received: accountNo={}, receiverId={}", account.getAccountNo(), receiverId);
        return AccountConverter.toVo(account);
    }

    @Override
    public PageResult<AccountVo> query(AccountQueryRequest request) {
        LambdaQueryWrapper<Account> query = new LambdaQueryWrapper<>();
        if (request.getCashierId() != null) {
            query.eq(Account::getCashierId, request.getCashierId());
        }
        if (request.getAccountStatus() != null) {
            query.eq(Account::getAccountStatus, AccountStatus.valueOf(request.getAccountStatus()));
        }
        if (request.getAccountDateFrom() != null) {
            query.ge(Account::getAccountDate, request.getAccountDateFrom());
        }
        if (request.getAccountDateTo() != null) {
            query.le(Account::getAccountDate, request.getAccountDateTo());
        }
        query.orderByDesc(Account::getAccountDate);

        Page<Account> page = request.toPage();
        IPage<Account> result = accountMapper.selectPage(page, query);
        List<AccountVo> vos = result.getRecords().stream()
                .map(AccountConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }
}
