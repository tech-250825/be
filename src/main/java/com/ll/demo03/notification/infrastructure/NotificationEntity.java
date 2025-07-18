package com.ll.demo03.notification.infrastructure;

import com.ll.demo03.member.infrastructure.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@Setter
@Getter
public class NotificationEntity {

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String message;

    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberEntity;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String payload;

}