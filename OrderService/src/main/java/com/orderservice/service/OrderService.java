package com.orderservice.service;

import com.orderservice.model.OrderRequest;
import com.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
