package com.ll.demo03.domain.image.controller;

import com.ll.demo03.domain.image.dto.WebhookEvent;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.image.repository.SseEmitterRepository;
import com.ll.demo03.domain.image.service.ImageService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/images")
@Slf4j  // 로깅 추가
public class ImageController {

    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    private final SseEmitterRepository sseEmitterRepository;

    @PostMapping(value = "/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter createImage(@RequestBody Map<String, String> data, Authentication authentication) {

        Optional<Member> optionalMember = memberRepository.findByEmail(authentication.getName());
        Member member = optionalMember.orElseThrow(() -> new RuntimeException("Member not found"));

        // 타임아웃을 10분으로 설정
        SseEmitter emitter = new SseEmitter(600000L); // 10분

        try {
//            // 초기 연결 성공 이벤트 전송
//            emitter.send(SseEmitter.event()
//                    .name("connect")
//                    .data("Connected successfully"));

            // 이미지 생성 시작 상태 전송
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data("이미지 생성 중..."));

            // 이미지 생성 요청 및 taskId 수신
            String taskId = imageService.createImage(data.get("prompt"), data.get("ratio"), "https://webhook.site/2f180bf9-0d86-4f1c-a966-6bd71962f387");
            log.info("Image creation started with taskId: {}", taskId);

            // taskId를 클라이언트에게 전송
            emitter.send(SseEmitter.event()
                    .name("task_id")
                    .data(taskId));

            // 추후 webhook 응답을 위해 emitter 저장
            Image image = new Image();
            image.setTaskid(taskId);
            imageRepository.save(image);
            sseEmitterRepository.save(taskId, emitter);

            // 타임아웃 및 완료 핸들러 설정
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
            log.error("Error during image creation process: ", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }


    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(@RequestBody WebhookEvent event) {
        try {
            log.info("Received webhook event: {}", event);

            // taskId와 이미지 URL들 추출
            String taskId = event.getData().getTask_id();
            List<String> imageUrls = event.getData().getOutput().getImage_urls();

            // DB에서 Image 엔티티 찾기
            Image image = imageRepository.findByTaskid(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image not found"));

            // 이미지 URL 저장
            if (imageUrls != null && imageUrls.size() >= 4) {
                image.setImage_url1(imageUrls.get(0));
                image.setImage_url2(imageUrls.get(1));
                image.setImage_url3(imageUrls.get(2));
                image.setImage_url4(imageUrls.get(3));
                imageRepository.save(image);
            }

            // SSE로 클라이언트에게 결과 전송
            SseEmitter emitter = sseEmitterRepository.get(taskId);
            if (emitter != null) {
                emitter.send(SseEmitter.event()
                        .name("result")
                        .data(imageUrls));
                emitter.complete();
                sseEmitterRepository.remove(taskId);
            }

            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/status/{taskId}")
//    public GlobalResponse<String> checkStatus(@PathVariable String taskId) {
//        log.info("Checking status for taskId: {}", taskId);
//        String imageUrl = imageService.checkTaskStatus(taskId);
//        return GlobalResponse.success(imageUrl);
//    }
//
//    @PostMapping("/upscale")
//    public GlobalResponse<String> upscaleImage(@RequestBody Map<String, String> data) {
//        String originTaskId = data.get("originTaskId");
//        int index = Integer.parseInt(data.get("index"));
//
//        log.info("Starting upscale for taskId: {} with index: {}", originTaskId, index);
//
//        String taskId = imageService.upscale(originTaskId, index);
//        String upscaledImageUrl = imageService.checkTaskStatus(taskId);
//
//        return GlobalResponse.success(upscaledImageUrl);
//    }
}