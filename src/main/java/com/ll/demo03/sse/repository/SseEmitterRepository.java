package com.ll.demo03.sse.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, String> taskToMemberMap = new ConcurrentHashMap<>();

    public void save(String memberId, SseEmitter emitter) {
        emitters.put(memberId, emitter);
    }

    public void saveTaskMapping(String taskId, String memberId) {
        taskToMemberMap.put(taskId, memberId);
    }

    public SseEmitter get(String memberId) {
        return emitters.get(memberId);
    }

    public String getMemberIdByTaskId(String taskId) {
        return taskToMemberMap.get(taskId);
    }

    public SseEmitter getByTaskId(String taskId) {
        String memberId = taskToMemberMap.get(taskId);
        if (memberId != null) {
            return emitters.get(memberId);
        }
        return null;
    }

    public void removeTaskMapping(String taskId) {
        taskToMemberMap.remove(taskId);
    }

    public void remove(String memberId) {
        emitters.remove(memberId);
    }
}