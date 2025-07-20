package com.ll.demo03.notification.service;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.notification.controller.port.NotificationService;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.domain.Notification;
import com.ll.demo03.notification.service.port.NotificationRepository;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;


    public List<NotificationResponse> getByMember(Member member) {
        return notificationRepository.findByMemberId(member.getId())
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long notificationId, Member member) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다: " + notificationId));

        if (!notification.getMember().equals(member)) {
            throw new AccessDeniedException("해당 알림에 접근 권한이 없습니다");
        }

        notificationRepository.delete(notification);
    }
}