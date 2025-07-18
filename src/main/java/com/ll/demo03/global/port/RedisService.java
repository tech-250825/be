package com.ll.demo03.global.port;

public interface RedisService {
    void setValue(String key, String value);
    String getValue(String key);
    void deleteValue(String key);
    void pushToImageQueue(String taskId);
}

