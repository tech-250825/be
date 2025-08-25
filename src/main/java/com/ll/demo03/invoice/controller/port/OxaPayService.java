package com.ll.demo03.invoice.controller.port;

import com.ll.demo03.invoice.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.invoice.controller.response.OxaPayStatusResponse;

public interface OxaPayService {
    void createInvoice(OxaPayInvoiceRequest request, Long memberId);
    OxaPayStatusResponse getPaymentStatus(String trackId, Long memberId);
    void handlePaymentCallback(Object callbackData);
}
