package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.UGC.infrastructure.UGCEntity;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VideoTaskJpaRepository extends JpaRepository<VideoTaskEntity, Long>, JpaSpecificationExecutor<VideoTaskEntity> {
    Optional<VideoTaskEntity> findById(Long taskId);

    void deleteByMemberId(Long memberId);

    Slice<VideoTaskEntity> findByMember(MemberEntity member, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThan(MemberEntity member, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(MemberEntity member, LocalDateTime createdAt);


}