package com.ll.demo03.domain.notification.service;

import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public List<NotificationResponse> getNotificationsByMemberId(Long memberId) {
        List<Notification> notifications = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotificationsByMemberId(Long memberId) {
        List<Notification> notifications = notificationRepository.findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(memberId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotification(Long notificationId, Long memberId) {
        Notification notification = findNotificationByIdAndValidateMember(notificationId, memberId);
        return convertToDto(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = findNotificationByIdAndValidateMember(notificationId, memberId);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        List<Notification> unreadNotifications = notificationRepository.findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(memberId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }


    @Transactional
    public void deleteNotification(Long notificationId, Long memberId) {
        Notification notification = findNotificationByIdAndValidateMember(notificationId, memberId);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteByMemberId(memberId);
    }

    private NotificationResponse convertToDto(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .status(notification.getStatus().name())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .payload(notification.getPayload())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private Notification findNotificationByIdAndValidateMember(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다: " + notificationId));

        if (!notification.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 알림에 접근 권한이 없습니다");
        }

        return notification;
    }
}