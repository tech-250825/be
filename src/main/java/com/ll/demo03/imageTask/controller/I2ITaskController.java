package com.ll.demo03.imageTask.controller;

import com.ll.demo03.global.controller.request.I2IWebhookEvent;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.util.JsonParser;
import com.ll.demo03.imageTask.controller.port.I2ITaskService;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequestV2;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.webhook.I2IWebhookProcessor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/img2img")
@Slf4j
public class I2ITaskController {

    private final I2IWebhookProcessor i2IWebhookProcessor;
    private final I2ITaskService i2ITaskService;

    @PostMapping(value = "/create")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImages(
            @RequestBody I2ITaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        i2ITaskService.initate(request, member);
        return GlobalResponse.success();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse createImages(
            @RequestPart("request") String requestJson,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        I2ITaskRequestV2 request = JsonParser.parseJson(requestJson, I2ITaskRequestV2.class);

        Member member = principalDetails.user();
        i2ITaskService.initate(request, member, image);
        return GlobalResponse.success();
    }

    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody I2IWebhookEvent event) {
        i2IWebhookProcessor.processWebhookEvent(event);

        return GlobalResponse.success();
    }

}
