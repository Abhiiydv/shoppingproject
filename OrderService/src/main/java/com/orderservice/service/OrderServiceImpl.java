package com.orderservice.service;

import com.orderservice.entity.Order;
import com.orderservice.external.client.ProductService;
import com.orderservice.model.OrderRequest;
import com.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public long placeOrder(OrderRequest orderRequest) {

        //steps we will follow
        //1.save order
        //2. reduce quantity by reduceAPi from productService
        //3.payment service call

        //reduce quantity first from ProductService API call
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());


        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);

        return order.getId();
    }
}
