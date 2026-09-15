package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.OrderCancelRequest;
import com.smarthis.clinical.dto.request.OrderCreateRequest;
import com.smarthis.clinical.dto.request.OrderVerifyRequest;
import com.smarthis.clinical.dto.response.OrderVo;

import java.util.List;

public interface OrderService {
    void submit(Long id);

    OrderVo create(OrderCreateRequest request);

    OrderVo getById(Long id);

    void verify(Long id, OrderVerifyRequest request);

    void cancel(Long id, OrderCancelRequest request);

    void stop(Long id);

    List<OrderVo> listByPatient(Long patientId);

    List<OrderVo> listByAdmission(Long admissionId);
}
