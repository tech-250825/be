package com.ll.demo03.domain.notification.repository;

import com.ll.demo03.domain.notification.entity.Notification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteByMemberId(Long memberId);

    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Notification> findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(Long memberId);

    Slice<Notification> findByMemberIdAndIsReadFalse(Long memberId, PageRequest pageRequest);

    Slice<Notification> findByMemberIdAndIdLessThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<Notification> findByMemberIdAndIdGreaterThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<Notification> findByMemberId(Long memberId, PageRequest pageRequest);

    Slice<Notification> findByMemberIdAndIsReadFalseAndIdGreaterThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<Notification> findByMemberIdAndIsReadFalseAndIdLessThan(Long memberId, Long cursorId, PageRequest pageRequest);

    int countByMemberIdAndIdGreaterThan(Long id, Long id1);

    int countByMemberIdAndIdLessThan(Long id, Long id1);
}

