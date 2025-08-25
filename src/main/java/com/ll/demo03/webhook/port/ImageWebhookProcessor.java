package com.ll.demo03.webhook.port;

import com.ll.demo03.global.controller.request.ImageWebhookEvent;

import java.util.List;

public interface ImageWebhookProcessor<T> {
    void processWebhookEvent(ImageWebhookEvent event);
    void saveToDatabase(Long taskId, List<String> url);
    void handleFailed(Long taskId, String runpodId);
    void handleCompleted(Long taskId, ImageWebhookEvent event, String runpodId);
}