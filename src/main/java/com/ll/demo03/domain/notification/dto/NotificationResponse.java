package com.ll.demo03.domain.notification.dto;

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

    private String type;

    private String status;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;

    private Object payload;
}
