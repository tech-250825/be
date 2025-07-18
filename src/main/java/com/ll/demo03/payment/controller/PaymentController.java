package com.ll.demo03.payment.controller;


import com.ll.demo03.oauth.entity.PrincipalDetails;
import com.ll.demo03.payment.controller.request.PaymentConfirmRequest;
import com.ll.demo03.payment.controller.response.PaymentConfirmResponse;
import com.ll.demo03.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody PaymentConfirmRequest requestDto) {
        log.info("Payment confirmation request received: {}", requestDto);

        PaymentConfirmResponse responseDto = paymentService.confirmPayment(requestDto);


        return ResponseEntity.status(responseDto.getStatusCode()).body(responseDto);
    }


    @GetMapping("/fail")
    public String paymentFail(HttpServletRequest request, Model model) {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        log.error("Payment failed: code={}, message={}", failCode, failMessage);
        return "/fail";
    }
}