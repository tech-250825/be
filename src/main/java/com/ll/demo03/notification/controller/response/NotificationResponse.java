package com.ll.demo03.notification.controller.response;

import com.ll.demo03.notification.domain.Notification;
import com.ll.demo03.notification.infrastructure.NotificationEntity;
import com.ll.demo03.notification.infrastructure.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private long id;

    private NotificationType type;

    private LocalDateTime createdAt;

    private Object payload;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .payload(notification.getPayload())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
