package com.ll.demo03.mock;

import com.ll.demo03.global.port.RedisService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeRedisService implements RedisService {

    private final Map<String, String> keyValueStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> queues = new ConcurrentHashMap<>();
    private final List<String> publishedMessages = new ArrayList<>();

    @Override
    public void setValue(String key, String value) {
        keyValueStore.put(key, value);
    }

    @Override
    public String getValue(String key) {
        return keyValueStore.get(key);
    }

    @Override
    public void deleteValue(String key) {
        keyValueStore.remove(key);
    }

    @Override
    public void pushToQueue(String type, Long taskId) {
        String queueKey = type + ":queue";
        queues.computeIfAbsent(queueKey, k -> new LinkedList<>()).add(String.valueOf(taskId));
    }

    @Override
    public void removeFromQueue(String type, Long taskId) {
        String queueKey = type + ":queue";
        List<String> queue = queues.getOrDefault(queueKey, new LinkedList<>());
        queue.remove(String.valueOf(taskId));
    }

    @Override
    public void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, String url) {
        String fakeMessage = String.format("Published: memberId=%d, taskId=%d, prompt=%s, imageUrl=%s",
                memberId, taskId, prompt, url);
        publishedMessages.add(fakeMessage);
    }

    @Override
    public void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, java.util.List<String> urls) {
        String fakeMessage = String.format("Published: memberId=%d, taskId=%d, prompt=%s, imageUrls=%s",
                memberId, taskId, prompt, String.join(",", urls));
        publishedMessages.add(fakeMessage);
    }

    @Override
    public void publishNotificationToOtherServers(Long memberId, Long boardId, Long taskId, String prompt, String url) {
        String fakeMessage = String.format("Published: memberId=%d, boardId=%d, taskId=%d, prompt=%s, imageUrl=%s",
                memberId, boardId, taskId, prompt, url);
        publishedMessages.add(fakeMessage);
    }

    // 테스트 검증용 헬퍼 메서드들
    public boolean queueContains(String type, Long taskId) {
        List<String> queue = queues.getOrDefault(type + ":queue", List.of());
        return queue.contains(String.valueOf(taskId));
    }


    public List<String> getPublishedMessages() {
        return publishedMessages;
    }

    public void clear() {
        keyValueStore.clear();
        queues.clear();
        publishedMessages.clear();
    }
}
