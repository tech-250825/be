package com.ll.demo03.domain.sse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.notification.dto.NotificationMessage;
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

        // 통합된 응답 객체 생성
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("image", imageJson != null ? imageJson : "{}");
        notificationData.put("upscale", upscaleJson != null ? upscaleJson : "{}");
        notificationData.put("video", videoJson != null ? videoJson : "{}");

        try {
            // 하나의 이벤트로 전송
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
            NotificationMessage notification = parseNotificationMessage(msgBody);

            String memberKey = "sse:member:" + notification.getMemberId();
            List<String> sessionIds = redisTemplate.opsForList().range(memberKey, 0, -1);

            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    SseEmitter emitter = sseEmitterRepository.get(sessionId);
                    if (emitter != null) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(notification.getNotificationJson()));
                        } catch (Exception e) {
                            log.error("❌ SSE 전송 실패: sessionId={}, error={}", sessionId, e.getMessage());

                            // emitter 제거
                            sseEmitterRepository.remove(sessionId);

                            // Redis에서도 해당 세션 제거
                            redisTemplate.opsForList().remove(memberKey, 1, sessionId);
                        }
                    } else {
                        // emitter 없으면 Redis에서도 정리
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