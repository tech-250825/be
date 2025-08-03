package com.ll.demo03.global.port;

import com.ll.demo03.notification.controller.response.NotificationResponse;

import java.util.List;

public interface RedisService {
    void setValue(String key, String value);
    String getValue(String key);
    void deleteValue(String key);
    void pushToQueue(String type, Long taskId);
    void removeFromQueue(String type, Long taskId);
    void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, String url);
    void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, List<String> url);
    void publishNotificationToOtherServers(Long memberId, Long boardId, Long taskId, String prompt, String url);
}

