package com.ll.demo03.domain.videoTask.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.imageTask.dto.TaskOrImageResponse;
import com.ll.demo03.domain.imageTask.service.VideoTaskService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.imageTask.service.ImageMessageProducer;
import com.ll.demo03.domain.videoTask.dto.TaskOrVideoResponse;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.dto.VideoTaskRequest;
import com.ll.demo03.domain.videoTask.dto.VideoWebhookEvent;
import com.ll.demo03.domain.webhook.VideoWebhookProcessor;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/videos")
@Slf4j
public class VideoTaskController {

    private final VideoWebhookProcessor videoWebhookProcessor;
    private final ImageMessageProducer imageMessageProducer;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final VideoTaskService videoTaskService;

    @Value("${openai.api.key}")
    private String openAiApiKey;


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
        String prompt = videoTaskRequest.getPrompt();
        String newPrompt = modifyPrompt(videoTaskRequest.getPrompt());

        String finalPrompt = (newPrompt == null || newPrompt.isBlank()) ? prompt : newPrompt;

        VideoMessageRequest videoMessageRequest = new VideoMessageRequest();
        videoMessageRequest.setMemberId(member.getId());
        videoMessageRequest.setLora(videoTaskRequest.getLora());
        videoMessageRequest.setPrompt(finalPrompt);

        try {
            imageMessageProducer.sendVideoCreationMessage(videoMessageRequest);

            log.info("영상 생성 요청을 메시지 큐에 전송했습니다. MemberId: {}", member.getId());
            return GlobalResponse.success();
        } catch (Exception e) {
            log.error("영상 생성 요청 중 오류 발생: ", e);
            return GlobalResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String modifyPrompt(String prompt) {
        String openAiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        try {
            // 메시지 구성
            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "If the user's input is in Korean, translate it into natural English. " +
                            "If the input is already in English, do not change it. " +
                            "Do not add any style or artistic interpretation — just translate or preserve as is."
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "temperature", 0,
                    "messages", List.of(systemMessage, userMessage)
            );

            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(openAiUrl, HttpMethod.POST, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            JsonNode rootNode = objectMapper.readTree(responseBody);
            String result = rootNode.path("choices").get(0).path("message").path("content").asText().trim();

            log.info("OpenAI 프롬프트 개선 응답: {}", result);
            return result;

        } catch (Exception e) {
            log.error("OpenAI API 요청 실패: ", e);
            return prompt;
        }
    }

    @PostMapping("/webhook")
    public GlobalResponse handleWebhook(
            @RequestBody VideoWebhookEvent event) {

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

    @GetMapping("/queue")
    public long getQueueLength() {
        return redisTemplate.opsForList().size("video:queue");
    }
}
