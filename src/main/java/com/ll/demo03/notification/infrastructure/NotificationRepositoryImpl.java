package com.ll.demo03.notification.infrastructure;

import com.ll.demo03.notification.domain.Notification;
import com.ll.demo03.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public void delete(Notification notification){
        notificationJpaRepository.delete(NotificationEntity.from(notification));
    };

    @Override
    public Optional<Notification> findById(Long id){
       return notificationJpaRepository.findById(id).map(NotificationEntity::toModel);
    };

    @Override
    public List<Notification> findByMemberId(Long id) {
        return notificationJpaRepository.findByMemberId(id).stream()
                .map(NotificationEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByMemberId(Long memberId){
        notificationJpaRepository.deleteByMemberId(memberId);
    };


}
