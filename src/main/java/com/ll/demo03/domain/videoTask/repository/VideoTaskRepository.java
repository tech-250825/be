package com.ll.demo03.domain.videoTask.repository;

import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VideoTaskRepository extends JpaRepository<VideoTask, Long>, JpaSpecificationExecutor<VideoTask> {
    Optional<VideoTask> findById(Long taskId);

    void deleteByMemberId(Long memberId);

    Slice<VideoTask> findByMember(Member member, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThan(Member member, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(Member member, LocalDateTime createdAt);

}