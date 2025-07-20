package com.ll.demo03.notification.service.port;

import com.ll.demo03.notification.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    void delete(Notification notification);

    Optional<Notification> findById(Long notificationId);

    List<Notification> findByMemberId(Long id);

    void deleteByMemberId(Long memberId);
}
