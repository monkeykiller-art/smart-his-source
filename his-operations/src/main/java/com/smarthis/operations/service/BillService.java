package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.BillChargeItemRequest;
import com.smarthis.operations.dto.request.BillChargeRequest;
import com.smarthis.operations.dto.request.BillCreateRequest;
import com.smarthis.operations.dto.request.BillPaymentRequest;
import com.smarthis.operations.dto.request.BillQueryRequest;
import com.smarthis.operations.dto.request.BillRegistrationRequest;
import com.smarthis.operations.dto.request.BillRefundRequest;
import com.smarthis.operations.dto.request.BillVoidRequest;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillTransactionVo;
import com.smarthis.operations.dto.response.BillVo;

import java.util.List;

public interface BillService {
    BillVo createFromOrder(com.smarthis.operations.dto.request.BillOrderRequest request);
    BillVo voidOrderSource(com.smarthis.operations.dto.request.BillOrderCancelRequest request);
    BillVo create(BillCreateRequest request);
    BillVo getById(Long id);
    List<BillItemVo> listItems(Long id);
    List<BillTransactionVo> listTransactions(Long id);
    BillTransactionVo pay(Long id, BillPaymentRequest request);
    BillTransactionVo refund(Long id, BillRefundRequest request);
    BillVo voidBill(Long id, BillVoidRequest request);
    PageResult<BillVo> query(BillQueryRequest request);
    BillVo createFromRegistration(BillRegistrationRequest request);
    List<BillItemVo> addChargeItem(Long billId, BillChargeItemRequest request);
    List<BillItemVo> chargeItems(BillChargeRequest request);
}
