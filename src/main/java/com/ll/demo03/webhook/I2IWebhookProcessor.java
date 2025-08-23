package com.ll.demo03.webhook;

import com.ll.demo03.global.controller.request.I2IWebhookEvent;

public interface I2IWebhookProcessor {
    void processWebhookEvent(I2IWebhookEvent event);
    void saveToDatabase(Long taskId, String url);
    void handleFailed(Long taskId, String runpodId);
    void handleCompleted(Long taskId, I2IWebhookEvent event, String runpodId);
}
