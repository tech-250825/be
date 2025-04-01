package com.ll.demo03.domain.videoTask.controller;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.task.service.ImageMessageProducer;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.dto.VideoTaskRequest;
import com.ll.demo03.domain.videoTask.dto.VideoWebhookEvent;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.service.VideoTaskService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/videos")
@Slf4j
public class VideoTaskController {

    private final MemberRepository memberRepository;
    private final RabbitTemplate rabbitTemplate;
    private final VideoTaskService videoTaskService;
    private final ImageMessageProducer imageMessageProducer;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createVideos(
            @RequestBody VideoTaskRequest videoTaskRequest,
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

        VideoMessageRequest videoMessageRequest = new VideoMessageRequest();
        videoMessageRequest.setMemberId(member.getId());
        videoMessageRequest.setImageUrl(videoTaskRequest.getImageUrl());
        videoMessageRequest.setPrompt(videoTaskRequest.getPrompt());

        try {
            imageMessageProducer.sendVideoCreationMessage(videoMessageRequest);

            log.info("영상 생성 요청을 메시지 큐에 전송했습니다. MemberId: {}", member.getId());
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("영상 생성 요청 중 오류 발생: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody VideoWebhookEvent event) {
        if (!"123456".equals(secret)) {
            log.error("Unauthorized webhook request. Secret key mismatch or missing");
            return GlobalResponse.error(ErrorCode.ACCESS_DENIED);
        }

        try {
            log.info("Received webhook event: {}", event);
            videoTaskService.processWebhookEvent(event);

            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
