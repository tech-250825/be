package com.ll.demo03.notification.infrastructure;

import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.notification.domain.Notification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @CreatedDate
    private LocalDateTime createdAt;

    public static NotificationEntity from(Notification notification){
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.id=notification.getId();
        notificationEntity.type=notification.getType();
        notificationEntity.member=MemberEntity.from(notification.getMember());
        notificationEntity.payload=notification.getPayload();
        return notificationEntity;
    }

    public Notification toModel() {
        return Notification.builder()
                .id(id)
                .type(type)
                .member(member.toModel())
                .payload(payload)
                .createdAt(createdAt)
                .build();
    }

}