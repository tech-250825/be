package com.ll.demo03.notification.controller.response;

import com.ll.demo03.notification.infrastructure.NotificationStatus;
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

    private NotificationStatus status;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private Object payload;

}
