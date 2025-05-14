package com.ll.demo03.domain.notification.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class NotificationMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(String.valueOf(notification.getType()))
                .status(String.valueOf(notification.getStatus()))
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .payload(parsePayload(notification.getPayload()))
                .build();
    }

    private static Object parsePayload(String jsonPayload) {
        try {
            return objectMapper.readValue(jsonPayload, new TypeReference<Map<String, Object>>() {});
        } catch( JsonProcessingException e) {
            return jsonPayload; // fallback
        }
    }
}
