package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.operations.converter.DepositConverter;
import com.smarthis.operations.dto.request.DepositCreateRequest;
import com.smarthis.operations.dto.request.DepositQueryRequest;
import com.smarthis.operations.dto.request.DepositRefundRequest;
import com.smarthis.operations.dto.response.DepositVo;
import com.smarthis.operations.entity.Deposit;
import com.smarthis.operations.entity.DepositAccount;
import com.smarthis.operations.enums.DepositType;
import com.smarthis.operations.enums.PayMethod;
import com.smarthis.operations.mapper.DepositAccountMapper;
import com.smarthis.operations.mapper.DepositMapper;
import com.smarthis.operations.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final DepositMapper depositMapper;
    private final DepositAccountMapper depositAccountMapper;
    private final BizNoGenerator bizNoGenerator;
    @Override
    @Transactional
    public DepositVo create(DepositCreateRequest request) {
        DepositAccount account = getOrCreateAccount(request.getPatientId(), request.getAdmissionId());
        BigDecimal balanceBefore = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        Deposit deposit = new Deposit();
        deposit.setDepositNo(bizNoGenerator.next(BizNoType.DEPOSIT));
        deposit.setPatientId(request.getPatientId());
        deposit.setAdmissionId(request.getAdmissionId());
        deposit.setReceiptNo(request.getReceiptNo());
        deposit.setAmount(request.getAmount());
        deposit.setPayMethod(PayMethod.valueOf(request.getPayMethod()));
        deposit.setDepositType(DepositType.PAYMENT);
        deposit.setBalanceBefore(balanceBefore);
        deposit.setBalanceAfter(balanceBefore.add(request.getAmount()));
        deposit.setCashierId(request.getCashierId());
        deposit.setCashierName(request.getCashierName());
        deposit.setChargeTime(LocalDateTime.now());
        deposit.setRemark(request.getRemark());
        deposit.setDepositStatus("NORMAL");
        depositMapper.insert(deposit);

        account.setTotalDeposit(safeAdd(account.getTotalDeposit(), request.getAmount()));
        account.setBalance(balanceBefore.add(request.getAmount()));
        depositAccountMapper.updateById(account);

        log.info("Deposit created: depositNo={}, amount={}", deposit.getDepositNo(), request.getAmount());
        return DepositConverter.toVo(deposit);
    }

    @Override
    public DepositVo getById(Long id) {
        Deposit d = depositMapper.selectById(id);
        if (d == null || d.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        }
        return DepositConverter.toVo(d);
    }

    @Override
    public PageResult<DepositVo> query(DepositQueryRequest request) {
        LambdaQueryWrapper<Deposit> query = new LambdaQueryWrapper<>();
        query.eq(Deposit::getDeleted, 0);
        if (request.getPatientId() != null) {
            query.eq(Deposit::getPatientId, request.getPatientId());
        }
        if (request.getAdmissionId() != null) {
            query.eq(Deposit::getAdmissionId, request.getAdmissionId());
        }
        if (StringUtils.hasText(request.getDepositStatus())) {
            query.eq(Deposit::getDepositStatus, request.getDepositStatus());
        }
        query.orderByDesc(Deposit::getCreatedTime);
        Page<Deposit> page = request.toPage();
        IPage<Deposit> result = depositMapper.selectPage(page, query);
        List<DepositVo> vos = result.getRecords().stream().map(DepositConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DepositVo refund(Long id, DepositRefundRequest request) {
        Deposit deposit = depositMapper.selectById(id);
        if (deposit == null || deposit.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        }
        DepositAccount account = getOrCreateAccount(deposit.getPatientId(), deposit.getAdmissionId());
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(ErrorCode.PREPAID_INSUFFICIENT);
        }
        BigDecimal balanceBefore = account.getBalance();
        Deposit refund = new Deposit();
        refund.setDepositNo(bizNoGenerator.next(BizNoType.REFUND));
        refund.setPatientId(deposit.getPatientId());
        refund.setAdmissionId(deposit.getAdmissionId());
        refund.setAmount(request.getAmount());
        refund.setPayMethod(deposit.getPayMethod());
        refund.setDepositType(DepositType.REFUND);
        refund.setBalanceBefore(balanceBefore);
        refund.setBalanceAfter(balanceBefore.subtract(request.getAmount()));
        refund.setCashierId(request.getCashierId());
        refund.setCashierName(request.getCashierName());
        refund.setChargeTime(LocalDateTime.now());
        refund.setRemark(request.getRemark());
        refund.setDepositStatus("REFUNDED");
        depositMapper.insert(refund);
        account.setBalance(balanceBefore.subtract(request.getAmount()));
        depositAccountMapper.updateById(account);
        log.info("Deposit refund: amount={}", request.getAmount());
        return DepositConverter.toVo(refund);
    }

    private DepositAccount getOrCreateAccount(Long patientId, Long admissionId) {
        LambdaQueryWrapper<DepositAccount> q = new LambdaQueryWrapper<>();
        q.eq(DepositAccount::getPatientId, patientId)
         .eq(DepositAccount::getAdmissionId, admissionId)
         .eq(DepositAccount::getDeleted, 0);
        DepositAccount account = depositAccountMapper.selectOne(q);
        if (account == null) {
            account = new DepositAccount();
            account.setPatientId(patientId);
            account.setAdmissionId(admissionId);
            account.setTotalDeposit(BigDecimal.ZERO);
            account.setTotalCharged(BigDecimal.ZERO);
            account.setBalance(BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setAccountStatus("ACTIVE");
            depositAccountMapper.insert(account);
        }
        return account;
    }

    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        return (a != null ? a : BigDecimal.ZERO).add(b);
    }
}
