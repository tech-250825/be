package com.ll.demo03.videoTask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.controller.request.WebhookEvent;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.JsonParser;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.videoTask.controller.port.VideoTaskService;
import com.ll.demo03.videoTask.controller.request.I2VTaskRequest;
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
    private final MemberRepository memberRepository;

    @PostMapping(value = "/create/t2v")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createT2V(
            @RequestBody VideoTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
            Member member = principalDetails.user();
            videoTaskService.initateT2V(request, member);
            return GlobalResponse.success();
    }

    @PostMapping(value = "/create/i2v/v2")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createI2V(
            @RequestBody I2VTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
            Member member = principalDetails.user();
            videoTaskService.initateI2V(request, member);
            return GlobalResponse.success();
    }

    @PostMapping(value = "/create/i2v/v2/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createI2VWithBoard(
            @PathVariable Long boardId,
            @RequestBody I2VTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
            Member member = principalDetails.user();
            videoTaskService.initateI2V(request, member, boardId);
            return GlobalResponse.success();
    }

    @PostMapping(value = "/create/t2v/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createT2VWithBoard(
            @PathVariable Long boardId,
            @RequestBody VideoTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        videoTaskService.initateT2V(request, member, boardId);
        return GlobalResponse.success();
    }

    @PostMapping(value = "/create/i2v/v3/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse createI2VFromLatestFrame(
            @PathVariable Long boardId,
            @RequestBody I2VTaskRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        videoTaskService.initateI2VFromLatestFrame(request, member, boardId);
        return GlobalResponse.success();
    }

    @PostMapping("/webhook")
    public GlobalResponse handleT2VWebhook(
            @RequestBody WebhookEvent event) {
            log.info("Received webhook event: {}", event);
            videoWebhookProcessor.processWebhookEvent(event);

            return GlobalResponse.success();
    }

    @DeleteMapping("/{taskId}")
    public GlobalResponse delete(@PathVariable Long taskId) {
        videoTaskService.delete(taskId);
        return GlobalResponse.success("삭제 완료되었습니다. ");
    }

    @GetMapping("/public")
    public GlobalResponse handle(
            CursorBasedPageable cursorBasedPageable) {
        Member member = memberRepository.findById(1L).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        PageResponse<List<TaskOrVideoResponse>> result = videoTaskService.getMyTasks(member, cursorBasedPageable);
        return GlobalResponse.success(result);
    }

    @GetMapping("/task")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse handle(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {
            Member member = principalDetails.user();

            PageResponse<List<TaskOrVideoResponse>> result = videoTaskService.getMyTasks(member, cursorBasedPageable);
            return GlobalResponse.success(result);
    }

    @GetMapping("/board/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalResponse<PageResponse<List<TaskOrVideoResponse>>> getVideoTasksByBoardId(
            @PathVariable Long boardId,
            CursorBasedPageable cursorBasedPageable) {
        
        PageResponse<List<TaskOrVideoResponse>> result = videoTaskService.getVideoTasksByBoardId(boardId, cursorBasedPageable);
        return GlobalResponse.success(result);
    }
}