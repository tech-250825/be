package com.ll.demo03.sse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.notification.controller.response.NotificationMessage;
import com.ll.demo03.notification.controller.response.NotificationResponse;
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

    public void registerSession(String memberId, String sessionId) {
        redisTemplate.opsForList().rightPush("sse:member:" + memberId, sessionId);
    }

    @GetMapping("/{memberId}")
    public SseEmitter connect(HttpServletRequest request, @PathVariable("memberId") String memberId) throws IOException {
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
        System.out.println("=== Redis Connection Debug ===");

        try {
            // Redis 연결 팩토리 정보 출력
            RedisConnectionFactory connectionFactory = redisMessageListenerContainer.getConnectionFactory();
            System.out.println("Connection Factory: " + connectionFactory);
            System.out.println("Connection Factory Class: " + connectionFactory.getClass());

            if (connectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
                System.out.println("Host: " + lettuceFactory.getHostName());
                System.out.println("Port: " + lettuceFactory.getPort());
                System.out.println("Password: " + (lettuceFactory.getPassword() != null ? "SET" : "NOT_SET"));
            }

            // 실제 연결 테스트
            RedisConnection connection = connectionFactory.getConnection();
            System.out.println("Connection SUCCESS: " + connection);
            connection.close();

            // 연결이 성공하면 리스너 등록
            redisMessageListenerContainer.addMessageListener((message, pattern) -> {
                String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
                NotificationMessage notificationMessage = parseNotificationMessage(msgBody);

                NotificationResponse response;
                try {
                    response = objectMapper.readValue(notificationMessage.getNotificationJson(), NotificationResponse.class);
                } catch (JsonProcessingException e) {
                    log.error("NotificationResponse 파싱 실패: {}", e.getMessage());
                    return;
                }

                String sendJson;
                try {
                    sendJson = objectMapper.writeValueAsString(response);
                } catch (JsonProcessingException e) {
                    log.error("NotificationResponse 직렬화 실패: {}", e.getMessage());
                    sendJson = notificationMessage.getNotificationJson();
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

            System.out.println("=== Redis Listener 등록 완료 ===");

        } catch (Exception e) {
            System.out.println("Redis Connection Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== Redis 연결 실패로 리스너 등록 건너뜀 ===");
        }
    }

    private NotificationMessage parseNotificationMessage(String message) {
        try {
            return objectMapper.readValue(message, NotificationMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 파싱 실패: " + message, e);
        }
    }
}
