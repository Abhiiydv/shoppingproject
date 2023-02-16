package com.paymentservice.service;

import com.paymentservice.entity.TransactionDetails;
import com.paymentservice.model.PaymentMode;
import com.paymentservice.model.PaymentRequest;
import com.paymentservice.model.PaymentResponse;
import com.paymentservice.repository.TransactionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details: {}", paymentRequest);

        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction Completed with Id: {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(String orderId) {

        log.info("getting payment details for orderId" + orderId);

        TransactionDetails transactionDetails = transactionDetailsRepository.findByOrderId(Long.valueOf(orderId));
        PaymentResponse paymentResponse = null;
        if(transactionDetails!=null){
             paymentResponse = PaymentResponse.builder()
                    .paymentId(transactionDetails.getId())
                    .paymentDate(transactionDetails.getPaymentDate())
                    .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                    .orderId(transactionDetails.getOrderId())
                    .amount(transactionDetails.getAmount())
                    .status(transactionDetails.getPaymentStatus())
                    .build();

        }

        return paymentResponse;

    }
}
