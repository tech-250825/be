package com.ll.demo03.sse.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(String memberId, SseEmitter emitter) {
        emitters.put(memberId, emitter);
    }

    public SseEmitter get(String memberId) {
        return emitters.get(memberId);
    }

    public void remove(String memberId) {
        emitters.remove(memberId);
    }
}