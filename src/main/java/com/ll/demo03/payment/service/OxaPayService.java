package com.ll.demo03.payment.service;

import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.payment.controller.request.OxaPayInvoiceRequest;
import com.ll.demo03.payment.controller.response.OxaPayStatusResponse;
import com.ll.demo03.payment.domain.Currency;
import com.ll.demo03.payment.domain.Status;
import com.ll.demo03.payment.infrastructure.PaymentEntity;
import com.ll.demo03.payment.infrastructure.PaymentRepository;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OxaPayService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate = new RestTemplate();
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    @Value("${oxapay.merchant.api-key:sandbox}")
    private String merchantApiKey;

    @Value("${oxapay.api.base-url:https://api.oxapay.com}")
    private String apiBaseUrl;

    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    public void createInvoice(OxaPayInvoiceRequest request, Member member) {
        try {
            String orderId = "ORD-" + System.currentTimeMillis();

            JSONObject requestBody = new JSONObject();
            requestBody.put("merchant", merchantApiKey);
            requestBody.put("amount", request.getAmount());
            requestBody.put("currency", request.getCurrency() != null ? request.getCurrency() : "TRX");
            requestBody.put("orderId", orderId);
            requestBody.put("description", "Purchase Credits");
            requestBody.put("callbackUrl", serverDomain + "/api/oxapay/webhook");
            requestBody.put("returnUrl", serverDomain + "/api/oxapay/success");
            requestBody.put("lifeTime", 30);

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
                String payLink = (String) responseBody.get("payLink");
                
                // Payment 엔티티에 저장
                PaymentEntity paymentEntity = new PaymentEntity();
                paymentEntity.setTrackId(trackId);
                paymentEntity.setMember(member);
                paymentEntity.setAmount(request.getAmount().toString());
                paymentEntity.setStatus(Status.PENDING);
                paymentEntity.setCurrency(Currency.valueOf(request.getCurrency() != null ? request.getCurrency() : "TRX"));
                paymentEntity.setApprovedAt(LocalDateTime.now());
                
                paymentRepository.save(paymentEntity);
                
                logger.info("OxaPay invoice created and saved: orderId={}, trackId={}", orderId, trackId);
            } else {
                throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }

    public OxaPayStatusResponse getPaymentStatus(String trackId, Member member) {
        try {
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
                return OxaPayStatusResponse.error("결제 상태 조회 실패", "INQUIRY_FAILED", 500);
            }

        } catch (Exception e) {
            logger.error("Payment status inquiry failed: {}", e.getMessage(), e);

            return OxaPayStatusResponse.error(
                "결제 상태 조회 중 오류가 발생했습니다: " + e.getMessage(),
                "PAYMENT_STATUS_INQUIRY_FAILED",
                500
            );
        }
    }

    public void handlePaymentCallback(Object callbackData) {
        logger.info("Processing OxaPay payment callback: {}", callbackData);
        
        // 여기서 실제 비즈니스 로직 처리
        // 예: 데이터베이스에 결제 상태 업데이트, 주문 처리 등
    }
}