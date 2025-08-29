package com.ll.demo03.invoice.controller.response;

import com.ll.demo03.invoice.domain.Currency;
import com.ll.demo03.invoice.domain.Invoice;
import com.ll.demo03.invoice.domain.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InvoiceListResponse {
    private final Long id;
    private final String trackId;
    private final BigDecimal amount;
    private final Currency currency;
    private final Status status;
    private final LocalDateTime approvedAt;

    public static InvoiceListResponse from(Invoice invoice) {
        return InvoiceListResponse.builder()
                .id(invoice.getId())
                .trackId(invoice.getTrackId())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .approvedAt(invoice.getApprovedAt())
                .build();
    }

    public static List<InvoiceListResponse> from(List<Invoice> invoices) {
        return invoices.stream()
                .map(InvoiceListResponse::from)
                .toList();
    }
}