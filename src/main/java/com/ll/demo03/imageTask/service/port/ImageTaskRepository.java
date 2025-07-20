package com.ll.demo03.imageTask.service.port;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;


public interface ImageTaskRepository {
    void deleteByMemberId(Long memberId);

    Slice<ImageTask> findByMember(Member creator, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThan(Member creator, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(Member creator, LocalDateTime createdAt);

    ImageTask save(ImageTask imageTask);

    Slice<ImageTask> findAll(Specification<ImageTask> spec, PageRequest pageRequest);

    Optional<ImageTask> findById(Long taskId);
}
