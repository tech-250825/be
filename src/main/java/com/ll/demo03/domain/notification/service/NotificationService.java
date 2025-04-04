package com.ll.demo03.domain.notification.service;

public interface NotificationService {
    void sendNotification(Long memberId, String eventName);
}

