package com.ll.demo03.invoice.controller.port;

import com.ll.demo03.invoice.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.invoice.controller.response.OxaPayStatusResponse;
import com.ll.demo03.invoice.controller.response.OxaPayInvoiceResponse;

public interface OxaPayService {
    OxaPayInvoiceResponse createInvoice(OxaPayInvoiceRequest request, Long memberId);
    OxaPayStatusResponse getPaymentStatus(String trackId, Long memberId);
    void handlePaymentCallback(Object callbackData);
}
