package com.ll.demo03.invoice.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.invoice.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.invoice.controller.response.OxaPayStatusResponse;
import com.ll.demo03.invoice.service.OxaPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/oxapay")
@Slf4j
@RequiredArgsConstructor
public class OxaPayController {

    private final OxaPayService oxaPayService;

    @PostMapping("/invoice")
    public GlobalResponse createInvoice(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody OxaPayInvoiceRequest request) {
        Member member = principalDetails.user();

        oxaPayService.createInvoice(request, member);

        return GlobalResponse.success();
    }

    @GetMapping("/status/{trackId}")
    public GlobalResponse<OxaPayStatusResponse> getPaymentStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String trackId) {
        Member member = principalDetails.user();

        OxaPayStatusResponse response = oxaPayService.getPaymentStatus(trackId, member);

        return GlobalResponse.success(response);
    }

    @PostMapping("/webhook")
    public GlobalResponse paymentCallback(@RequestBody Object callbackData) {
        oxaPayService.handlePaymentCallback(callbackData);
        
        return GlobalResponse.success();
    }

    @GetMapping("/success")
    public String paymentSuccess(HttpServletRequest request) {
        log.info("OxaPay payment success page accessed");
        return "redirect:/payment-success";
    }
}