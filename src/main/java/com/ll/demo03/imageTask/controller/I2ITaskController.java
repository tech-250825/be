package com.ll.demo03.imageTask.controller;

import com.ll.demo03.global.controller.request.I2IWebhookEvent;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.port.I2ITaskService;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequest;
import com.ll.demo03.imageTask.controller.response.TaskOrI2IResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.webhook.port.I2IWebhookProcessor;
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
@RequestMapping("/api/img2img")
@Slf4j
public class I2ITaskController {

    private final I2IWebhookProcessor i2IWebhookProcessor;
    private final I2ITaskService i2ITaskService;

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createImages(
            @RequestBody I2ITaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        i2ITaskService.initate(request, member);
        return GlobalResponse.success();
    }

    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody I2IWebhookEvent event) {
        i2IWebhookProcessor.processWebhookEvent(event);

        return GlobalResponse.success();
    }

    @GetMapping("/task")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse handle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {

        Member member = principalDetails.user();
        PageResponse<List<TaskOrI2IResponse>>  result = i2ITaskService.getMyTasks(member, cursorBasedPageable);

        return GlobalResponse.success(result);
    }
}
