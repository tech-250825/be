package com.ll.demo03.sse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.notification.controller.response.NotificationMessage;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.handler.NotificationHandler;
import com.ll.demo03.notification.handler.NotificationHandlerRegistry;
import com.ll.demo03.notification.service.NotificationMessageFactory;
import com.ll.demo03.sse.repository.SseEmitterRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRepository sseEmitterRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ObjectMapper objectMapper;
    private final NotificationHandlerRegistry notificationHandlerRegistry;

    public void registerSession(String memberId, String sessionId) {
        redisTemplate.opsForList().rightPush("sse:member:" + memberId, sessionId);
    }

    @GetMapping("/{memberId}")
    public SseEmitter connect(HttpServletRequest request, @PathVariable("memberId") String memberId) throws IOException {
        String sessionId = request.getSession(true).getId();
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        sseEmitterRepository.save(sessionId, emitter);
        redisTemplate.opsForList().rightPush("sse:member:" + memberId, sessionId);

        emitter.onCompletion(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        emitter.onTimeout(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data("sse연결이 되었습니다.", MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToNotifications() {
        System.out.println("=== Redis Connection Debug ===");

        try {
            redisMessageListenerContainer.addMessageListener((message, pattern) -> {
                String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);

                try {
                    JsonNode root = objectMapper.readTree(msgBody);
                    String type = root.get("type").asText();

                    NotificationHandler handler = notificationHandlerRegistry.getHandler(type);
                    if (handler != null) {
                        handler.handle(msgBody);
                    } else {
                        log.warn("⚠ 핸들러 없음: {}", type);
                    }
                } catch (Exception e) {
                    log.error("❌ 메시지 처리 실패: {}", e.getMessage(), e);
                }

            }, new ChannelTopic("sse-notification-channel"));

        } catch (Exception e) {
            System.out.println("Redis Connection Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== Redis 연결 실패로 리스너 등록 건너뜀 ===");
        }
    }

}
