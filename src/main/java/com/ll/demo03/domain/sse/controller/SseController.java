package com.ll.demo03.domain.sse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.notification.dto.NotificationMessage;
import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRepository sseEmitterRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ObjectMapper objectMapper;

    public void registerSession(String memberId, String sessionId) {
        redisTemplate.opsForList().rightPush("sse:member:" + memberId, sessionId);
    }

    @GetMapping("/{memberId}")
    public SseEmitter connect(HttpServletRequest request, @PathVariable String memberId) throws IOException {
        String sessionId = request.getSession(true).getId();
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        sseEmitterRepository.save(sessionId, emitter);
        registerSession(memberId, sessionId);

        emitter.onCompletion(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        emitter.onTimeout(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        // Redis에서 데이터 가져오기
        String imageJson = redisTemplate.opsForValue().get("notification:image:" + memberId);
        String upscaleJson = redisTemplate.opsForValue().get("notification:upscale:" + memberId);
        String videoJson = redisTemplate.opsForValue().get("notification:video:" + memberId);

        // JSON -> 객체 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationResponse image = imageJson != null ? objectMapper.readValue(imageJson, NotificationResponse.class) : new NotificationResponse();
        NotificationResponse upscale = upscaleJson != null ? objectMapper.readValue(upscaleJson, NotificationResponse.class) : new NotificationResponse();
        NotificationResponse video = videoJson != null ? objectMapper.readValue(videoJson, NotificationResponse.class) : new NotificationResponse();

        // 응답 데이터 조합
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("image", image);
        notificationData.put("upscale", upscale);
        notificationData.put("video", video);

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notificationData, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToNotifications() {
        redisMessageListenerContainer.addMessageListener((message, pattern) -> {
            String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationMessage notificationMessage = parseNotificationMessage(msgBody);

            // 1) Redis에서 받은 notificationJson을 Notification 객체로 변환
            Notification notification;
            try {
                notification = objectMapper.readValue(notificationMessage.getNotificationJson(), Notification.class);
            } catch (JsonProcessingException e) {
                log.error("NotificationJson 파싱 실패: {}", e.getMessage());
                return;
            }

            // 2) Notification 내부 payload(String) -> Map 등으로 변환
            Map<String, Object> payloadObj = null;
            try {
                if (notification.getPayload() != null) {
                    payloadObj = objectMapper.readValue(notification.getPayload(), new TypeReference<Map<String, Object>>() {});
                }
            } catch (JsonProcessingException e) {
                log.error("Payload 파싱 실패: {}", e.getMessage());
            }

            // 3) Notification 객체에서 payload를 Map으로 교체할 새로운 DTO 생성
            NotificationResponse response = new NotificationResponse(
                    notification.getId(),
                    notification.getType(),
                    notification.getStatus(),
                    notification.getMessage(),
                    notification.isRead(),
                    notification.getMember(),
                    notification.getCreatedAt(),
                    payloadObj
            );

            // 4) DTO를 다시 JSON으로 직렬화해서 클라이언트에 보냄
            String sendJson;
            try {
                sendJson = objectMapper.writeValueAsString(response);
            } catch (JsonProcessingException e) {
                log.error("NotificationResponse 직렬화 실패: {}", e.getMessage());
                sendJson = notificationMessage.getNotificationJson(); // 실패 시 원본 그대로 보내기
            }

            String memberKey = "sse:member:" + notificationMessage.getMemberId();
            List<String> sessionIds = redisTemplate.opsForList().range(memberKey, 0, -1);

            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    SseEmitter emitter = sseEmitterRepository.get(sessionId);
                    if (emitter != null) {
                        try {
                            emitter.send(SseEmitter.event().data(sendJson));
                        } catch (Exception e) {
                            log.error("❌ SSE 전송 실패: sessionId={}, error={}", sessionId, e.getMessage());
                            sseEmitterRepository.remove(sessionId);
                            redisTemplate.opsForList().remove(memberKey, 1, sessionId);
                        }
                    } else {
                        redisTemplate.opsForList().remove(memberKey, 1, sessionId);
                    }
                }
            }
        }, new ChannelTopic("sse-notification-channel"));

    }

    private NotificationMessage parseNotificationMessage(String message) {
        try {
            return objectMapper.readValue(message, NotificationMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 파싱 실패: " + message, e);
        }
    }


}