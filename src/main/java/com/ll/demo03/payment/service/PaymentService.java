package com.ll.demo03.payment.service;

import com.ll.demo03.payment.controller.request.PaymentConfirmRequest;
import com.ll.demo03.payment.controller.response.PaymentConfirmResponse;
import com.ll.demo03.global.util.TossPaymentApiClient;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TossPaymentApiClient tossPaymentApiClient;

    @Value("${payment.toss.secret-key}")
    private String widgetSecretKey;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest requestDto) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("orderId", requestDto.getOrderId());
            requestBody.put("amount", requestDto.getAmount());
            requestBody.put("paymentKey", requestDto.getPaymentKey());

            JSONObject response = tossPaymentApiClient.confirmPayment(requestBody, widgetSecretKey);

            return PaymentConfirmResponse.fromJsonObject(response, 200);
        } catch (Exception e) {
            logger.error("Payment confirmation failed: {}", e.getMessage(), e);

            JSONObject errorResponse = new JSONObject();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("code", "PAYMENT_CONFIRMATION_FAILED");

            return PaymentConfirmResponse.fromJsonObject(errorResponse, 500);
        }
    }
}