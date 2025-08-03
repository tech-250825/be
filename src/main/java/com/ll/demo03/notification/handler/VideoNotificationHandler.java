package com.ll.demo03.notification.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.notification.controller.response.NotificationMessage;
import com.ll.demo03.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoNotificationHandler implements NotificationHandler {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Override
    public void handle(String jsonMessage) {
        try {
            NotificationMessage message = objectMapper.readValue(jsonMessage, NotificationMessage.class);
            long memberId = message.getMemberId();
            sseService.sendToEmitters(memberId, message);
        } catch (Exception e) {
            log.error("❌ VideoNotificationHandler 파싱/전송 실패: {}", e.getMessage(), e);
        }
    }
}
