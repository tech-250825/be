package com.ll.demo03.payment.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class OxaPayInvoiceRequest {
    private Double amount;
    private String currency;

    public OxaPayInvoiceRequest(Double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
}