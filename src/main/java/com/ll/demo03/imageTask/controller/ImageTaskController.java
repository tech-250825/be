package com.ll.demo03.imageTask.controller;

import com.ll.demo03.global.controller.request.ImageWebhookEvent;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.webhook.ImageWebhookProcessor;
import com.ll.demo03.webhook.ImageWebhookProcessorImpl;
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
    private final ImageTaskService imageTaskService;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImages(
            @RequestBody ImageTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
            Member member = principalDetails.user();
            imageTaskService.initate(request, member);
            return GlobalResponse.success();
    }


    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody ImageWebhookEvent event) {
            log.info("Received webhook event: {}", event);
            imageWebhookProcessor.processWebhookEvent(event);

            return GlobalResponse.success();
    }

    @GetMapping("/task")
    public GlobalResponse handle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {

        Member member = principalDetails.user();
        PageResponse<List<TaskOrImageResponse>> result = imageTaskService.getMyTasks(member, cursorBasedPageable);

        return GlobalResponse.success(result);
    }

    @DeleteMapping("/{taskId}")
    public GlobalResponse delete(@PathVariable Long taskId) {
        imageTaskService.delete(taskId);
        return GlobalResponse.success("삭제 완료되었습니다.");
    }
}
