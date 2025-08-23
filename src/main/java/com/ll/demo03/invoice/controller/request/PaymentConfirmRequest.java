package com.ll.demo03.invoice.controller.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private String amount;

    @Override
    public String toString() {
        return "PaymentConfirmRequestDto{" +
                "paymentKey='" + paymentKey + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}