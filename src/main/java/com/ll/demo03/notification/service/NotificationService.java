package com.ll.demo03.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.notification.controller.response.NotificationMessage;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.infrastructure.NotificationEntity;
import com.ll.demo03.notification.infrastructure.NotificationJpaRepository;
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

    private final NotificationJpaRepository notificationJpaRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;


    public PageResponse<List<NotificationResponse>> getNotificationsByMemberId(Long memberId, CursorBasedPageable pageable) {
        Slice<NotificationEntity> notificationsPage;

        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 - 최신순 정렬
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationJpaRepository.findByMemberId(memberId, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지 (더 최신 데이터)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationJpaRepository.findByMemberIdAndIdGreaterThan(memberId, cursorId, pageRequest);
        } else {
            // 다음 페이지 (더 이전 데이터)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            notificationsPage = notificationJpaRepository.findByMemberIdAndIdLessThan(memberId, cursorId, pageRequest);
        }

        return buildPageResponse(notificationsPage, pageable, memberId);
    }

    private PageResponse<List<NotificationResponse>> buildPageResponse(Slice<NotificationEntity> notificationsPage, CursorBasedPageable pageable, Long memberId) {
        if (!notificationsPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<NotificationEntity> notificationEntities = notificationsPage.getContent();
        List<NotificationResponse> responseList = notificationEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 첫 번째와 마지막 항목
        NotificationEntity firstNotificationEntity = notificationEntities.get(0);
        NotificationEntity lastNotificationEntity = notificationEntities.get(notificationEntities.size() - 1);

        // 이전 페이지 커서 생성
        boolean hasPrevPage = notificationJpaRepository.countByMemberIdAndIdGreaterThan(
                memberId, firstNotificationEntity.getId()) > 0;
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstNotificationEntity.getId()), hasPrevPage);

        // 다음 페이지 커서 생성
        boolean hasNextPage = notificationJpaRepository.countByMemberIdAndIdLessThan(
                memberId, lastNotificationEntity.getId()) > 0;
        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastNotificationEntity.getId()), hasNextPage);

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }


    @Transactional
    public void deleteNotification(Long notificationId, Long memberId) {
        NotificationEntity notificationEntity = findNotificationByIdAndValidateMember(notificationId, memberId);
        notificationJpaRepository.delete(notificationEntity);
    }


    private NotificationResponse convertToDto(NotificationEntity notificationEntity) {
        return NotificationResponse.builder()
                .id(notificationEntity.getId())
                .message(notificationEntity.getMessage())
                .status(notificationEntity.getStatus())
                .type(notificationEntity.getType())
                .isRead(notificationEntity.isRead())
                .payload(notificationEntity.getPayload())
                .createdAt(notificationEntity.getCreatedAt())
                .build();
    }

    private NotificationEntity findNotificationByIdAndValidateMember(Long notificationId, Long memberId) {
        NotificationEntity notificationEntity = notificationJpaRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다: " + notificationId));

        if (!notificationEntity.getMemberEntity().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 알림에 접근 권한이 없습니다");
        }

        return notificationEntity;
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