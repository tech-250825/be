package com.ll.demo03.domain.notification.service;

import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SseEmitterRepository sseEmitterRepository;

    @Async
    @Override
    public void sendNotification(Long memberId, String eventName) {
        String memberIdStr = String.valueOf(memberId);
        SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName));
            } catch (Exception e) {
                log.error("❌ SSE 전송 실패: {}", e.getMessage(), e);
                sseEmitterRepository.remove(memberIdStr);
            }
        } else {
            log.warn("❗ SSE Emitter 없음: memberId={}", memberId);
        }
    }
}
