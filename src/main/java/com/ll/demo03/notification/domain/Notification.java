package com.ll.demo03.notification.domain;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.notification.infrastructure.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Notification {

    private Long id;

    private NotificationType type;

    private Member member;

    private String payload;

    private LocalDateTime createdAt;

    @Builder
    public Notification(Long id, NotificationType type, Member member, String payload, LocalDateTime createdAt){
        this.id = id;
        this.type = type;
        this.member = member;
        this.payload = payload;
        this.createdAt = createdAt;
    }
}
