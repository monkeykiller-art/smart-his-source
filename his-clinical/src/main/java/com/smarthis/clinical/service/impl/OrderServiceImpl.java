package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.OrderConverter;
import com.smarthis.clinical.dto.request.OrderCancelRequest;
import com.smarthis.clinical.dto.request.OrderCreateRequest;
import com.smarthis.clinical.dto.request.OrderItemRequest;
import com.smarthis.clinical.dto.request.OrderVerifyRequest;
import com.smarthis.clinical.dto.response.OrderVo;
import com.smarthis.clinical.entity.Order;
import com.smarthis.clinical.entity.OrderItem;
import com.smarthis.clinical.mapper.OrderItemMapper;
import com.smarthis.clinical.mapper.OrderMapper;
import com.smarthis.clinical.service.OrderService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int MONEY_SCALE = 4;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final BizNoGenerator bizNoGenerator;
    private final com.smarthis.clinical.client.OperationsClient operationsClient;

    @Override
    @Transactional
    public void submit(Long id) {
        Order order = orderMapper.selectByIdForUpdate(id);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_INVALID);
        if ("SUBMITTED".equals(order.getOrderStatus())) return;
        if (!"DRAFT".equals(order.getOrderStatus()) || order.getEncounterId() == null) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, id).eq(OrderItem::getDeleted, 0);
        List<OrderItem> items = orderItemMapper.selectList(query);
        if (items.isEmpty()) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        for (OrderItem item : items) {
            if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0
                    || item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }
            var amount = item.getUnitPrice().multiply(item.getQuantity()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (amount.scale() > 4 || amount.precision() - amount.scale() > 14) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }
        }
        order.setOrderStatus("SUBMITTED");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public OrderVo create(OrderCreateRequest request) {
        Order order = OrderConverter.toEntity(request);
        order.setOrderNo(bizNoGenerator.next(BizNoType.ORDER));
        order.setOrderTime(LocalDateTime.now());
        orderMapper.insert(order);

        List<OrderItem> items = new ArrayList<>();
        int seq = 1;
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderConverter.toItemEntity(order.getId(), seq++, itemReq);
            orderItemMapper.insert(item);
            items.add(item);
        }

        log.info("Order created: orderNo={}, patientId={}, itemCount={}", order.getOrderNo(), order.getPatientId(), items.size());
        OrderVo vo = OrderConverter.toVo(order);
        vo.setItems(items.stream().map(OrderConverter::toItemVo).toList());
        return vo;
    }

    @Override
    public OrderVo getById(Long id) {
        Order order = getEntity(id);
        OrderVo vo = OrderConverter.toVo(order);

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, id)
                .eq(OrderItem::getDeleted, 0)
                .orderByAsc(OrderItem::getItemSeq);
        List<OrderItem> items = orderItemMapper.selectList(itemQuery);
        vo.setItems(items.stream().map(OrderConverter::toItemVo).toList());
        return vo;
    }

    @Override
    @Transactional
    public void verify(Long id, OrderVerifyRequest request) {
        Order order = getEntity(id);
        if (!"SUBMITTED".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setOrderStatus("VERIFIED");
        order.setVerifyNurseId(request.getNurseId());
        order.setVerifyTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Order verified: id={}, nurseId={}", id, request.getNurseId());
    }

    @Override
    @Transactional
    public void cancel(Long id, OrderCancelRequest request) {
        Order order = orderMapper.selectByIdForUpdate(id);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_INVALID);
        if ("COMPLETED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (!"DRAFT".equals(order.getOrderStatus()) || order.getBillId() != null) {
            java.util.Map<String, Object> source = new java.util.HashMap<>();
            source.put("orderId", id);
            source.put("patientId", order.getPatientId());
            source.put("encounterId", order.getEncounterId());
            source.put("deptId", order.getDeptId());
            source.put("billId", order.getBillId());
            source.put("reason", request.getReason() == null || request.getReason().isBlank() ? "取消医嘱" : request.getReason());
            var response = operationsClient.voidOrderSource(source);
            if (response == null || response.getCode() != 200) throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }
        order.setOrderStatus("CANCELLED");
        order.setCancelNurseId(request.getNurseId());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(request.getReason());
        orderMapper.updateById(order);
        log.info("Order cancelled: id={}", id);
    }

    @Override
    @Transactional
    public void stop(Long id) {
        Order order = getEntity(id);
        if (!"VERIFIED".equals(order.getOrderStatus()) && !"EXECUTING".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setOrderStatus("STOPPED");
        orderMapper.updateById(order);
        log.info("Order stopped: id={}", id);
    }

    @Override
    public List<OrderVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getPatientId, patientId)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getOrderTime);
        List<Order> orders = orderMapper.selectList(query);
        return orders.stream().map(o -> {
            OrderVo vo = OrderConverter.toVo(o);
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, o.getId())
                    .eq(OrderItem::getDeleted, 0)
                    .orderByAsc(OrderItem::getItemSeq);
            List<OrderItem> items = orderItemMapper.selectList(itemQuery);
            vo.setItems(items.stream().map(OrderConverter::toItemVo).toList());
            return vo;
        }).toList();
    }

    @Override
    public List<OrderVo> listByAdmission(Long admissionId) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getAdmissionId, admissionId)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getOrderTime);
        List<Order> orders = orderMapper.selectList(query);
        return orders.stream().map(o -> {
            OrderVo vo = OrderConverter.toVo(o);
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, o.getId())
                    .eq(OrderItem::getDeleted, 0)
                    .orderByAsc(OrderItem::getItemSeq);
            List<OrderItem> items = orderItemMapper.selectList(itemQuery);
            vo.setItems(items.stream().map(OrderConverter::toItemVo).toList());
            return vo;
        }).toList();
    }

    private Order getEntity(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ORDER_INVALID);
        }
        return order;
    }
}
