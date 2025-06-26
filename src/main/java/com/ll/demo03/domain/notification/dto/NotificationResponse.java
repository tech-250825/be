package com.ll.demo03.domain.notification.dto;

import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
