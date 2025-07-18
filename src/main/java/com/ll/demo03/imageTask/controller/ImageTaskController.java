package com.ll.demo03.imageTask.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.request.ImageWebhookEvent;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.service.ImageTaskServiceImpl;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.entity.PrincipalDetails;
import com.ll.demo03.webhook.ImageWebhookProcessor;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
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
public class ImageTaskController {

    private final ImageWebhookProcessor imageWebhookProcessor;
    private final ImageTaskServiceImpl imageTaskService;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImages(
            @RequestBody ImageTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try{
            Member member = principalDetails.user();
            imageTaskService.initate(request, member);
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("영상 생성 요청 중 오류 발생: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody ImageWebhookEvent event) {

        try {
            log.info("Received webhook event: {}", event);
            imageWebhookProcessor.processWebhookEvent(event);

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
            PageResponse<List<TaskOrImageResponse>> result = imageTaskService.getMyTasks(member, cursorBasedPageable);

            return GlobalResponse.success(result);
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
