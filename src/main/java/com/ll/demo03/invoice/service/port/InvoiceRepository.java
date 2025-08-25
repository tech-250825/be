package com.ll.demo03.invoice.service.port;

import com.ll.demo03.invoice.domain.Invoice;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
}
