package com.ll.demo03.notification.controller.port;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getByMember(Member member);

    void delete(Long notificationId, Member member);
}
