package com.ll.demo03.domain.notification.dto;

import com.ll.demo03.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.Payload;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotificationMessage {
    private long memberId;

    private String notificationJson;
}
