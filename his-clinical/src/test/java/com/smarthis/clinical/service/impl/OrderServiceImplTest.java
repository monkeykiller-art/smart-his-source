package com.smarthis.clinical.service.impl;

import com.smarthis.clinical.client.OperationsClient;
import com.smarthis.clinical.dto.request.OrderCancelRequest;
import com.smarthis.clinical.dto.request.OrderCreateRequest;
import com.smarthis.clinical.dto.request.OrderItemRequest;
import com.smarthis.clinical.entity.Order;
import com.smarthis.clinical.entity.OrderItem;
import com.smarthis.clinical.mapper.OrderMapper;
import com.smarthis.clinical.mapper.OrderItemMapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.support.BizNoGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class OrderServiceImplTest {
    private final OrderMapper orders = mock(OrderMapper.class);
    private final OrderItemMapper items = mock(OrderItemMapper.class);
    private final OperationsClient client = mock(OperationsClient.class);
    private final OrderServiceImpl service = new OrderServiceImpl(orders, items, mock(BizNoGenerator.class), client);

    @Test
    void rejectsIncompleteMedicinePrescription() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrderType("MEDICINE");
        OrderItemRequest item = new OrderItemRequest();
        item.setItemName("阿莫西林");
        request.setItems(List.of(item));
        assertThrows(BusinessException.class, () -> service.create(request));
        verify(orders, never()).insert(any(Order.class));
    }

    @Test
    void submitsDraftOnceAndRejectsUnpricedItems() {
        Order order = order("DRAFT");
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        OrderItem item = new OrderItem();
        item.setQuantity(BigDecimal.ONE);
        when(items.selectList(any())).thenReturn(List.of(item));
        assertThrows(BusinessException.class, () -> service.submit(81L));
        assertEquals("DRAFT", order.getOrderStatus());
        item.setUnitPrice(new BigDecimal("0.10"));
        service.submit(81L);
        service.submit(81L);
        assertEquals("SUBMITTED", order.getOrderStatus());
        verify(orders, times(1)).updateById(order);
    }

    @Test
    void acceptsDatabaseMoneyScaleWithoutRejectingAValidOrder() {
        Order order = order("DRAFT");
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        OrderItem item = new OrderItem();
        item.setQuantity(new BigDecimal("1.0000"));
        item.setUnitPrice(new BigDecimal("1.0000"));
        when(items.selectList(any())).thenReturn(List.of(item));

        service.submit(81L);

        assertEquals("SUBMITTED", order.getOrderStatus());
        verify(orders).updateById(order);
    }

    @Test
    void doesNotCancelOrderIfBillCannotBeVoided() {
        Order order = order("SUBMITTED");
        order.setBillId(91L);
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        when(client.voidOrderSource(any())).thenThrow(new RuntimeException("paid bill"));
        assertThrows(RuntimeException.class, () -> service.cancel(81L, new OrderCancelRequest()));
        assertEquals("SUBMITTED", order.getOrderStatus());
        verify(orders, never()).updateById(any(Order.class));
    }

    @Test
    void voidsUnpaidBillBeforeCancellingOrder() {
        Order order = order("SUBMITTED");
        order.setBillId(91L);
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        when(client.voidOrderSource(any())).thenReturn(ApiResponse.ok(Map.<String, Object>of("id", 91L)));
        service.cancel(81L, new OrderCancelRequest());
        var sequence = inOrder(client, orders);
        sequence.verify(client).voidOrderSource(any());
        sequence.verify(orders).updateById(order);
        assertEquals("CANCELLED", order.getOrderStatus());
    }

    private Order order(String status) {
        Order order = new Order();
        order.setId(81L);
        order.setEncounterId(31L);
        order.setOrderStatus(status);
        return order;
    }

    @Test
    void cancelsPendingBillingBySourceEvenWhenLocalBillIdIsMissing() {
        Order order = order("SUBMITTED");
        when(orders.selectByIdForUpdate(81L)).thenReturn(order);
        when(client.voidOrderSource(any())).thenReturn(ApiResponse.ok(Map.<String, Object>of("id", 91L)));
        service.cancel(81L, new OrderCancelRequest());
        verify(client).voidOrderSource(argThat(request -> request.get("orderId").equals(81L)));
        assertEquals("CANCELLED", order.getOrderStatus());
    }
}
