package com.ll.demo03.notification.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.service.NotificationServiceImpl;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    private final NotificationServiceImpl notificationService;

    @GetMapping
    @Operation(summary = "모든 알림 조회", description = "사용자의 모든 알림을 조회합니다.")
    public GlobalResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Member member = principalDetails.user();
        List<NotificationResponse> notifications = notificationService.getByMember(member);
        return GlobalResponse.success(notifications);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "사용자의 알림을 삭제합니다.")
    public GlobalResponse<String> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Member member = principalDetails.user();
        notificationService.delete(notificationId, member);
        return GlobalResponse.success("알람이 삭제되었습니다. ");
    }


}
