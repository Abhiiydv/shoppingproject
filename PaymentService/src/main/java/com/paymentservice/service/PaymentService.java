package com.paymentservice.service;

import com.paymentservice.model.PaymentRequest;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);
}
