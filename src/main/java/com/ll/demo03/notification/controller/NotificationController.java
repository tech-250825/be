package com.ll.demo03.notification.controller;

import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.service.NotificationService;
import com.ll.demo03.oauth.entity.PrincipalDetails;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "알림 API", description = "알림 조회/읽음 처리/삭제")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자의 모든 알림 목록 조회 (커서 기반 페이징)
     */
    @GetMapping
    @Operation(summary = "모든 알림 조회", description = "사용자의 모든 알림을 조회합니다.")
    public ResponseEntity<PageResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable) {
        Long memberId = principalDetails.user().getId();
        PageResponse<List<NotificationResponse>> notifications = notificationService.getNotificationsByMemberId(memberId, cursorBasedPageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "사용자의 알림을 삭제합니다.")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long memberId = principalDetails.user().getId();
        notificationService.deleteNotification(notificationId, memberId);
        return ResponseEntity.ok().build();
    }


}
