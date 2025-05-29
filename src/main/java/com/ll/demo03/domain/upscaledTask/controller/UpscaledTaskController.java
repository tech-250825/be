package com.ll.demo03.domain.upscaledTask.controller;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.task.service.ImageMessageProducer;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequest;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequestMessage;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleWebhookEvent;
import com.ll.demo03.domain.upscaledTask.service.UpscaleTaskService;
import com.ll.demo03.domain.webhook.UpscaleWebhookProcessor;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/upscale-images")
@Slf4j
public class UpscaledTaskController {

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    private final ImageMessageProducer imageMessageProducer;
    private final UpscaleWebhookProcessor upscaleWebhookProcessor;
    private final StringRedisTemplate redisTemplate;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImage(
            @RequestBody UpscaleTaskRequest upscaleTaskRequest,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        Long memberId=member.getId();
        int credit = member.getCredit();
        if (credit <= 0) {
            throw new CustomException(ErrorCode.NO_CREDIT);
        }

        UpscaleTaskRequestMessage upscaleTaskRequestMessage= UpscaleTaskRequestMessage.builder()
                .memberId(memberId)
                .taskId(upscaleTaskRequest.getTaskId())
                .index(upscaleTaskRequest.getIndex())
                .webhookUrl( webhookUrl + "/api/upscale-images/webhook").build();

        try {
            imageMessageProducer.sendImageUpscaleMessage(upscaleTaskRequestMessage);

            log.info("업스케일 요청을 메시지 큐에 전송했습니다. MemberId: {}", member.getId());
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("업스케일 요청 중 오류 발생: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody UpscaleWebhookEvent event) {
        if (!"123456".equals(secret)) {
            log.error("Unauthorized webhook request. Secret key mismatch or missing");
            return GlobalResponse.error(ErrorCode.ACCESS_DENIED);
        }

        try {
            log.info("Received webhook event: {}", event);
            upscaleWebhookProcessor.processWebhookEvent(event);

            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/queue")
    public long getQueueLength() {
        return redisTemplate.opsForList().size("upscale:queue");
    }
}
