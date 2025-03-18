package com.ll.demo03.domain.task.repository;

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
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(String taskId, SseEmitter emitter) {
        emitters.put(taskId, emitter);
    }

    public SseEmitter get(String taskId) {
        return emitters.get(taskId);
    }

    public void remove(String taskId) {
        emitters.remove(taskId);
    }
}
