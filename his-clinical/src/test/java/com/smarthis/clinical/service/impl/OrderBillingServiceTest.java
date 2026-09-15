package com.smarthis.clinical.service.impl;

import com.smarthis.clinical.client.OperationsClient;
import com.smarthis.clinical.entity.Order;
import com.smarthis.clinical.entity.OrderItem;
import com.smarthis.clinical.mapper.OrderMapper;
import com.smarthis.clinical.mapper.OrderItemMapper;
import com.smarthis.common.model.ApiResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class OrderBillingServiceTest {
    private final OrderMapper orders = mock(OrderMapper.class);
    private final OrderItemMapper items = mock(OrderItemMapper.class);
    private final OperationsClient client = mock(OperationsClient.class);
    private final OrderBillingService service = new OrderBillingService(orders, items, client);

    @Test
    void keepsFailedBillingPendingThenRetriesWithSameOrderSource() {
        Order order = order("SUBMITTED");
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        OrderItem item = new OrderItem();
        item.setId(82L);
        item.setItemName("测试检验");
        item.setQuantity(new BigDecimal("3"));
        item.setUnitPrice(new BigDecimal("0.10"));
        when(items.selectList(any())).thenReturn(List.of(item));
        when(client.createOrderBill(any())).thenThrow(new RuntimeException("unavailable"))
                .thenReturn(ApiResponse.ok(Map.<String, Object>of("id", 91L)));
        assertFalse(service.sync(81L));
        assertNull(order.getBillId());
        assertEquals("SUBMITTED", order.getOrderStatus());
        assertTrue(service.sync(81L));
        assertEquals(91L, order.getBillId());
        assertTrue(service.sync(81L));
        verify(client, times(2)).createOrderBill(argThat(request -> request.get("orderId").equals(81L)
                && request.get("encounterId").equals(31L)));
    }

    @Test
    void neverBillsDraftOrCancelledOrders() {
        when(orders.selectByIdForUpdate(81L)).thenReturn(order("DRAFT"), order("CANCELLED"));
        assertFalse(service.sync(81L));
        assertFalse(service.sync(81L));
        verifyNoInteractions(client, items);
    }

    private Order order(String status) {
        Order order = new Order();
        order.setId(81L);
        order.setPatientId(10L);
        order.setEncounterId(31L);
        order.setDeptId(20L);
        order.setOrderStatus(status);
        return order;
    }
}
