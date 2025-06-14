package com.ll.demo03.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.notification.dto.NotificationMessage;
import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;


    public PageResponse<List<NotificationResponse>> getNotificationsByMemberId(Long memberId, CursorBasedPageable pageable) {
        Slice<Notification> notificationsPage;

        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 - 최신순 정렬
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberId(memberId, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지 (더 최신 데이터)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberIdAndIdGreaterThan(memberId, cursorId, pageRequest);
        } else {
            // 다음 페이지 (더 이전 데이터)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberIdAndIdLessThan(memberId, cursorId, pageRequest);
        }

        return buildPageResponse(notificationsPage, pageable, memberId);
    }

    public PageResponse<List<NotificationResponse>> getUnreadNotificationsByMemberId(Long memberId, CursorBasedPageable pageable) {
        Slice<Notification> notificationsPage;

        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberIdAndIsReadFalse(memberId, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberIdAndIsReadFalseAndIdGreaterThan(memberId, cursorId, pageRequest);
        } else {
            // 다음 페이지
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationRepository.findByMemberIdAndIsReadFalseAndIdLessThan(memberId, cursorId, pageRequest);
        }

        return buildPageResponse(notificationsPage, pageable, memberId);
    }

    private PageResponse<List<NotificationResponse>> buildPageResponse(Slice<Notification> notificationsPage, CursorBasedPageable pageable, Long memberId) {
        if (!notificationsPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Notification> notifications = notificationsPage.getContent();
        List<NotificationResponse> responseList = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 첫 번째와 마지막 항목
        Notification firstNotification = notifications.get(0);
        Notification lastNotification = notifications.get(notifications.size() - 1);

        // 이전 페이지 커서 생성
        boolean hasPrevPage = notificationRepository.countByMemberIdAndIdGreaterThan(
                memberId, firstNotification.getId()) > 0;
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstNotification.getId()), hasPrevPage);

        // 다음 페이지 커서 생성
        boolean hasNextPage = notificationRepository.countByMemberIdAndIdLessThan(
                memberId, lastNotification.getId()) > 0;
        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastNotification.getId()), hasNextPage);

        return new PageResponse<>(responseList, prevCursor, nextCursor);
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
                .status(notification.getStatus())
                .type(notification.getType())
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

    public void publishNotificationToOtherServers(String memberIdStr, String notificationJson) {
        NotificationMessage message = new NotificationMessage();
        message.setMemberId(Long.parseLong(memberIdStr));
        message.setNotificationJson(notificationJson); // 또는 직렬화된 Notification 객체

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend("sse-notification-channel", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("❌ Redis Publish 실패: memberId={}, error={}", memberIdStr, e.getMessage());
        }
    }

}