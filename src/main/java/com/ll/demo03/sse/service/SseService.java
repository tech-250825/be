package com.ll.demo03.sse.service;

import com.ll.demo03.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendToEmitters(long memberId, Object notificationMessage) {
        String memberKey = "sse:member:" + memberId;
        List<String> sessionIds = redisTemplate.opsForList().range(memberKey, 0, -1);

        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                SseEmitter emitter = sseEmitterRepository.get(sessionId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().data(notificationMessage));
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
    }
}
