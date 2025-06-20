package com.ll.demo03.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

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

    private Member member;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private Object payload;

}
