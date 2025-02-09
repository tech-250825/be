package com.ll.demo03.domain.image.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Component
public class SseEmitterRepository {
    // ConcurrentHashMap을 사용하여 thread-safe하게 emitter 관리
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Emitter 저장
     */
    public void save(String taskId, SseEmitter emitter) {
        emitters.put(taskId, emitter);
    }

    /**
     * Emitter 조회
     */
    public SseEmitter get(String taskId) {
        return emitters.get(taskId);
    }

    /**
     * Emitter 제거
     */
    public void remove(String taskId) {
        emitters.remove(taskId);
    }

    /**
     * 모든 Emitter 조회
     */
    public Map<String, SseEmitter> getAll() {
        return Collections.unmodifiableMap(emitters);
    }

    /**
     * 완료된 Emitter 정리
     */
    public void cleanup() {
        emitters.forEach((taskId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
            } catch (IOException e) {
                emitters.remove(taskId);
            }
        });
    }
}
