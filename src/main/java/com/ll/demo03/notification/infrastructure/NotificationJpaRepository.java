package com.ll.demo03.notification.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    void deleteByMemberId(Long memberId);

    List<NotificationEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<NotificationEntity> findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(Long memberId);

    Slice<NotificationEntity> findByMemberIdAndIsReadFalse(Long memberId, PageRequest pageRequest);

    Slice<NotificationEntity> findByMemberIdAndIdLessThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<NotificationEntity> findByMemberIdAndIdGreaterThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<NotificationEntity> findByMemberId(Long memberId, PageRequest pageRequest);

    Slice<NotificationEntity> findByMemberIdAndIsReadFalseAndIdGreaterThan(Long memberId, Long cursorId, PageRequest pageRequest);

    Slice<NotificationEntity> findByMemberIdAndIsReadFalseAndIdLessThan(Long memberId, Long cursorId, PageRequest pageRequest);

    int countByMemberIdAndIdGreaterThan(Long id, Long id1);

    int countByMemberIdAndIdLessThan(Long id, Long id1);
}

