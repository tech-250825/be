package com.ll.demo03.invoice.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class OxaPayInvoiceRequest {
    private BigDecimal amount;
    private String currency;
}