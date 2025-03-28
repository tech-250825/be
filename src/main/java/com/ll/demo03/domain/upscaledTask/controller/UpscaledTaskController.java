package com.ll.demo03.domain.upscaledTask.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleImageUrlResponse;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequest;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleWebhookEvent;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import com.ll.demo03.domain.upscaledTask.service.UpscaleTaskService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/upscale-images")
@Slf4j
public class UpscaledTaskController {

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    private final UpscaleTaskService upscaleTaskService;
    private final MemberRepository memberRepository;
    private final UpscaleTaskRepository upscaleTaskRepository;
    private final ImageRepository imageRepository;
    private final SseEmitterRepository sseEmitterRepository;

    @PostMapping(value = "/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter createImage(
            @RequestBody UpscaleTaskRequest upscaleTaskRequest,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        int credit = member.getCredit();
        if (credit <= 0) {
            throw new CustomException(ErrorCode.NO_CREDIT);
        }
        credit -= 1;
        member.setCredit(credit);
        memberRepository.save(member);

        SseEmitter emitter = new SseEmitter(600000L); // 10분
        try {

            emitter.send(SseEmitter.event()
                    .name("status")
                    .data("이미지 생성 중..."));

            String response = upscaleTaskService.createUpscaleImage( upscaleTaskRequest.getTaskId(), upscaleTaskRequest.getIndex(), "https://3fbc-1-216-36-137.ngrok-free.app"+"/api/upscale-images/webhook");
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                String taskId = rootNode.path("data").path("task_id").asText();

                System.out.println("Extracted task_id: " + taskId);

                emitter.send(SseEmitter.event()
                        .name("task_id")
                        .data(taskId));

                UpscaleTask upscaleTask = new UpscaleTask();
                upscaleTask.setNewTaskId(taskId);
                upscaleTask.setMember(member);

                upscaleTaskRepository.save(upscaleTask);
                sseEmitterRepository.save(taskId, emitter);

                emitter.onTimeout(() -> {
                    log.warn("SSE connection timed out for taskId: {}", taskId);
                    sseEmitterRepository.remove(taskId);
                    emitter.complete();
                });

                emitter.onCompletion(() -> {
                    log.info("SSE connection completed for taskId: {}", taskId);
                    sseEmitterRepository.remove(taskId);
                });

            } catch (Exception e) {
                log.error("JSON 파싱 오류: ",  e);
                emitter.completeWithError(e);
            }
        } catch (Exception e) {
            log.error("이미지 생성 중 오류 발생: ", e);
            emitter.completeWithError(e);
        }
        return emitter;
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

            if (!"completed".equals(event.getData().getStatus())) {
                log.info("Task not yet completed, status: {}", event.getData().getStatus());
                return GlobalResponse.success();
            }

            String taskId = event.getData().getTask_id();
            System.out.println("Received taskId: " + taskId);
            String imageUrl = event.getData().getOutput().getImage_url();
            System.out.println("Received imageUrls: " + imageUrl);

            if (imageUrl == null || imageUrl.isEmpty()) {
                log.info("No mypage URLs available yet for taskId: {}", taskId);
                return GlobalResponse.success();
            }

            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image not found"));

            Image image = Image.ofUpscale(imageUrl, upscaleTask);
            image.setImgIndex(0);
            imageRepository.save(image);

            SseEmitter emitter = sseEmitterRepository.get(taskId);
            if (emitter != null) {
                UpscaleImageUrlResponse response = new UpscaleImageUrlResponse(imageUrl, taskId);
                emitter.send(SseEmitter.event()
                        .name("result")
                        .data(response));
                emitter.complete();
                sseEmitterRepository.remove(taskId);
            }

            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
