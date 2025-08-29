package com.ll.demo03.invoice.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.invoice.controller.port.OxaPayService;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.invoice.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.invoice.controller.response.InvoiceListResponse;
import com.ll.demo03.invoice.controller.response.OxaPayStatusResponse;
import com.ll.demo03.invoice.controller.response.OxaPayInvoiceResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/oxapay")
@Slf4j
@RequiredArgsConstructor
public class OxaPayController {

    private final OxaPayService oxaPayService;

    @PostMapping("/invoice")
    public GlobalResponse<OxaPayInvoiceResponse> createInvoice(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody OxaPayInvoiceRequest request) {
        Long memberId = principalDetails.user().getId();

        OxaPayInvoiceResponse response = oxaPayService.createInvoice(request, memberId);

        return GlobalResponse.success(response);
    }

    @GetMapping("/status/{trackId}")
    public GlobalResponse<OxaPayStatusResponse> getPaymentStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String trackId) {
        Long memberId = principalDetails.user().getId();

        OxaPayStatusResponse response = oxaPayService.getPaymentStatus(trackId, memberId);

        return GlobalResponse.success(response);
    }

    @GetMapping("/invoices")
    public GlobalResponse<List<InvoiceListResponse>> getPaidInvoices(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long memberId = principalDetails.user().getId();

        List<InvoiceListResponse> response = oxaPayService.getPaidInvoices(memberId);

        return GlobalResponse.success(response);
    }

    @PostMapping("/webhook")
    public GlobalResponse paymentCallback(@RequestBody Object callbackData) {
        oxaPayService.handlePaymentCallback(callbackData);
        
        return GlobalResponse.success();
    }
}