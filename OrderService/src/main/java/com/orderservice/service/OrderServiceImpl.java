package com.orderservice.service;

import com.orderservice.entity.Order;
import com.orderservice.external.client.PaymentService;
import com.orderservice.external.client.ProductService;
import com.orderservice.external.request.PaymentRequest;
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

    @Autowired
    private PaymentService paymentService;

    public long placeOrder(OrderRequest orderRequest) {

        //steps we will follow
        //1.save order
        //2. reduce quantity by reduceAPi from productService
        //3.payment service call

        //reduce quantity first from ProductService API call
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Order request" +orderRequest);
        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);

        log.info("calling payment service");

        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Oder status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);


        log.info("Order placed successfully with Order Id " +order.getId());

        return order.getId();
    }
}
