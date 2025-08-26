package com.ll.demo03.invoice.service.port;

import com.ll.demo03.invoice.domain.Invoice;

import java.util.Optional;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findByTrackId(String trackId);
}
