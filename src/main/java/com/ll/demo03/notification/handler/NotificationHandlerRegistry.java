package com.ll.demo03.notification.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationHandlerRegistry {

    private final VideoNotificationHandler videoHandler;
    private final ImageNotificationHandler imageHandler;

    private final Map<String, NotificationHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        handlerMap.put("video", videoHandler);
        handlerMap.put("image", imageHandler);
    }

    public NotificationHandler getHandler(String type) {
        return handlerMap.get(type);
    }
}

