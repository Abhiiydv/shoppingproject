package com.orderservice.service;

import com.orderservice.entity.Order;
import com.orderservice.exception.CustomException;
import com.orderservice.external.client.PaymentService;
import com.orderservice.external.client.ProductService;
import com.orderservice.external.request.PaymentRequest;
import com.orderservice.model.OrderRequest;
import com.orderservice.model.OrderResponse;
import com.orderservice.model.PaymentResponse;
import com.orderservice.model.ProductResponse;
import com.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    public long placeOrder(OrderRequest orderRequest) {

        //steps we will follow
        //1.save order
        //2. reduce quantity by reduceAPi from productService
        //3.payment service call

        //reduce quantity first from ProductService API call
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

       // log.info("Order request" +orderRequest);
      //  log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);

       // log.info("calling payment service");

        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
          //  log.info("Payment done Successfully. Changing the Oder status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
          //  log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);


      //  log.info("Order placed successfully with Order Id " +order.getId());

        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        // TODO Auto-generated method stub
     //   log.info("Get order details for orderId : {}" +orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(()-> new CustomException("Order not found for Order Id :"+orderId,"NOT_FOUND",404));


       // log.info("Invoking Product service to fetch the product for id: {}",
               // order.getProductId());

         ProductResponse productResponse = restTemplate.getForObject(
          "http://PRODUCT-SERVICE/product/" + order.getProductId(),
          ProductResponse.class );

       // log.info("getting payment details for this order id : "+orderId);

            PaymentResponse paymentResponse =
                    restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+orderId ,PaymentResponse.class);
          log.info("payment response"+paymentResponse);
        OrderResponse.PaymentDetails paymentDetails = new OrderResponse.PaymentDetails();
            if(paymentResponse!=null) {

                 paymentDetails = OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentStatus(paymentResponse.getStatus())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .build();
            }
else
{
   log.info("somethging wrong with error");
}
          OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails
         .builder() .productName(productResponse.getProductName())
          .productId(productResponse.getProductId()) .build();

        OrderResponse orderResponse = OrderResponse.builder().OrderId(order.getId())
                .OrderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }

}
