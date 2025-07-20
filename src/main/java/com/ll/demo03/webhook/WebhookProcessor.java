package com.ll.demo03.webhook;

import com.ll.demo03.global.controller.request.WebhookEvent;

public interface WebhookProcessor<T> {
    void processWebhookEvent(WebhookEvent event);
    void saveToDatabase(Long taskId, String url);
    void handleFailed(Long taskId, String runpodId);
    void handleCompleted(Long taskId, WebhookEvent event, String runpodId);
}