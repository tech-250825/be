package com.ll.demo03.notification.infrastructure;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    void deleteByMemberId(Long memberId);

    List<NotificationEntity> findByMemberId(Long id);
}

