package com.ll.demo03.videoTask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.controller.request.WebhookEvent;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.port.VideoTaskService;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.webhook.VideoWebhookProcessorImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/videos")
@Slf4j
public class VideoTaskController {

    private final VideoWebhookProcessorImpl videoWebhookProcessor;
    private final VideoTaskService videoTaskService;

    @PostMapping(value = "/create/t2v")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createT2V(
            @RequestBody VideoTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
            Member member = principalDetails.user();
            videoTaskService.initateT2V(request, member);
            return GlobalResponse.success();

    }

    @PostMapping(value = "/create/i2v" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createI2V(
            @RequestPart("request") String requestJson,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try {
            // 이거 제거할것.
            ObjectMapper objectMapper = new ObjectMapper();
            VideoTaskRequest request = objectMapper.readValue(requestJson, VideoTaskRequest.class);

            Member member = principalDetails.user();
            videoTaskService.initateI2V(request, member, image);
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("I2V 처리 중 오류: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }


    @PostMapping("/webhook")
    public GlobalResponse handleT2VWebhook(
            @RequestBody WebhookEvent event) {
            log.info("Received webhook event: {}", event);
            videoWebhookProcessor.processWebhookEvent(event);

            return GlobalResponse.success();
    }


    @GetMapping("/task")
    public GlobalResponse handle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {

            Member member = principalDetails.user();
            PageResponse<List<TaskOrVideoResponse>> result = videoTaskService.getMyTasks(member, cursorBasedPageable);

            return GlobalResponse.success(result);

    }
}