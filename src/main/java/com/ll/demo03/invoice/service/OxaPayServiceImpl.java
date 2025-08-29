package com.ll.demo03.invoice.service;

import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.invoice.controller.port.OxaPayService;
import com.ll.demo03.invoice.controller.response.OxaPayInvoiceResponse;
import com.ll.demo03.invoice.domain.Currency;
import com.ll.demo03.invoice.domain.Invoice;
import com.ll.demo03.invoice.service.port.InvoiceRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.invoice.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.invoice.controller.response.OxaPayStatusResponse;
import com.ll.demo03.invoice.domain.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OxaPayServiceImpl implements OxaPayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;

    @Value("${oxapay.merchant.api-key:sandbox}")
    private String merchantApiKey;

    @Value("${oxapay.api.base-url:https://api.oxapay.com}")
    private String apiBaseUrl;

    @Value("${custom.webhook-url}")
    private String serverDomain;

    @Value("${server.frontend}")
    private String frontDomain;

    @Override
    public OxaPayInvoiceResponse createInvoice(OxaPayInvoiceRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String orderId = "ORD-" + System.currentTimeMillis();

        JSONObject requestBody = new JSONObject();
        requestBody.put("merchant", merchantApiKey);
        requestBody.put("amount", request.getAmount());
        requestBody.put("currency", request.getCurrency() != null ? request.getCurrency() : "TRX");
        requestBody.put("orderId", orderId);
        requestBody.put("description", "Purchase Credits");
        requestBody.put("callbackUrl", serverDomain + "/api/oxapay/webhook");
        requestBody.put("returnUrl", frontDomain);
        requestBody.put("lifeTime", 30);
        requestBody.put("sandbox", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JSONObject> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<JSONObject> response = restTemplate.postForEntity(
                apiBaseUrl + "/merchants/request",
                entity,
                JSONObject.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JSONObject responseBody = response.getBody();
            String trackId = (String) responseBody.get("trackId");

            Invoice invoice = Invoice.from(trackId, member, request.getAmount(),
                    Currency.valueOf(request.getCurrency()), Status.WAITING, null);
            invoiceRepository.save(invoice);

            return OxaPayInvoiceResponse.fromJsonObject(responseBody, response.getStatusCode().value());

        } else {
            return OxaPayInvoiceResponse.fromJsonObject(response.getBody(), response.getStatusCode().value());
        }
    }


    @Override
    public OxaPayStatusResponse getPaymentStatus(String trackId, Long memberId) {
            memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

            JSONObject requestBody = new JSONObject();
            requestBody.put("merchant", merchantApiKey);
            requestBody.put("trackId", trackId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<JSONObject> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JSONObject> response = restTemplate.postForEntity(
                apiBaseUrl + "/merchants/inquiry",
                entity,
                JSONObject.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return OxaPayStatusResponse.fromJsonObject(response.getBody(), 200);
            } else {
                throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }
    }

    @Override
    public void handlePaymentCallback(Object callbackData) {
        log.info("Received callback data: {}", callbackData);

        if (!(callbackData instanceof Map<?, ?> callbackMap)) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        String trackId = (String) callbackMap.get("trackId");
        String status = (String) callbackMap.get("status");
        String amountStr = (String) callbackMap.get("amount");
        Double paidAmount = Double.valueOf(amountStr);

        Invoice invoice = invoiceRepository.findByTrackId(trackId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if ("Paid".equalsIgnoreCase(status)) {
            Member member = invoice.getMember();

            int creditToAdd;
            if (paidAmount.intValue() == 10) {
                creditToAdd = 1100; // 10일 때만 특별 케이스
            } else {
                creditToAdd = paidAmount.intValue() * 100; // 그 외 일반 케이스
            }

            member.increaseCredit(creditToAdd);
            memberRepository.save(member);

            Invoice updatedInvoice = invoice.updateStatus(Status.PAID);
            invoiceRepository.save(updatedInvoice);
        }
    }
}