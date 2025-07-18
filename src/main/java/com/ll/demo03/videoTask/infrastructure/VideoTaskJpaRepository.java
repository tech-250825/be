package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VideoTaskJpaRepository extends JpaRepository<VideoTaskEntity, Long>, JpaSpecificationExecutor<VideoTaskEntity> {
    Optional<VideoTaskEntity> findById(Long taskId);

    void deleteByMemberId(Long memberId);

    Slice<VideoTaskEntity> findByMember(MemberEntity memberEntity, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThan(MemberEntity memberEntity, Long createdAt);

    boolean existsByMemberAndCreatedAtLessThan(MemberEntity memberEntity, Long createdAt);

    Slice<VideoTaskEntity> findAll(Specification<VideoTask> spec, PageRequest pageRequest);
}