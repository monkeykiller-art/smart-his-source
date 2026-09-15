package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.client.OperationsClient;
import com.smarthis.clinical.entity.Order;
import com.smarthis.clinical.entity.OrderItem;
import com.smarthis.clinical.mapper.OrderMapper;
import com.smarthis.clinical.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderBillingService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OperationsClient operationsClient;

    @Transactional
    public boolean sync(Long id) {
        Order order = orderMapper.selectByIdForUpdate(id);
        if (order == null) return false;
        if (order.getBillId() != null) return true;
        if (!List.of("SUBMITTED", "VERIFIED", "EXECUTING", "COMPLETED").contains(order.getOrderStatus())) return false;
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, id).eq(OrderItem::getDeleted, 0).orderByAsc(OrderItem::getItemSeq);
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", id);
        request.put("patientId", order.getPatientId());
        request.put("encounterId", order.getEncounterId());
        request.put("deptId", order.getDeptId());
        request.put("items", itemMapper.selectList(query).stream().map(item -> {
            Map<String, Object> line = new HashMap<>();
            line.put("orderItemId", item.getId());
            line.put("itemCode", item.getItemCode());
            line.put("itemName", item.getItemName());
            line.put("itemClass", item.getItemType());
            line.put("spec", item.getSpec());
            line.put("unit", item.getQuantityUnit());
            line.put("unitPrice", item.getUnitPrice());
            line.put("quantity", item.getQuantity());
            return line;
        }).toList());
        try {
            var response = operationsClient.createOrderBill(request);
            if (response != null && response.getCode() == 200 && response.getData() != null
                    && response.getData().get("id") != null) {
                order.setBillId(Long.valueOf(response.getData().get("id").toString()));
            }
        } catch (Exception exception) {
            // The committed submitted order remains a durable retry source.
            log.warn("Order billing pending; orderId={}", id);
        }
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return order.getBillId() != null;
    }
}
