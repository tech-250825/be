package com.ll.demo03.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.notification.controller.response.BatchNotificationMessage;
import com.ll.demo03.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageNotificationHandler implements NotificationHandler {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Override
    public void handle(String jsonMessage) {
        try {
            BatchNotificationMessage batch = objectMapper.readValue(jsonMessage, BatchNotificationMessage.class);
            long memberId = batch.getMemberId();
            sseService.sendToEmitters(memberId, batch);
        } catch (Exception e) {
            log.error("❌ ImageNotificationHandler 파싱/전송 실패: {}", e.getMessage(), e);
        }
    }
}
