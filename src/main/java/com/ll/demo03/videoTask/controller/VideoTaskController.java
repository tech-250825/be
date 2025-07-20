package com.ll.demo03.videoTask.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/images")
@Slf4j
public class VideoTaskController {

    private final VideoWebhookProcessorImpl videoWebhookProcessor;
    private final VideoTaskService videoTaskService;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImages(
            @RequestBody VideoTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try{
            Member member = principalDetails.user();
            videoTaskService.initate(request, member);
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("영상 생성 요청 중 오류 발생: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody WebhookEvent event) {

        try {
            log.info("Received webhook event: {}", event);
            videoWebhookProcessor.processWebhookEvent(event);

            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/task")
    public GlobalResponse handle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {
        try {

            Member member = principalDetails.user();
            PageResponse<List<TaskOrVideoResponse>> result = videoTaskService.getMyTasks(member, cursorBasedPageable);

            return GlobalResponse.success(result);
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}