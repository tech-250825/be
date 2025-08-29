package com.ll.demo03.imageTask.service.port;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;


public interface ImageTaskRepository {
    void deleteByMemberId(Long memberId);

    Slice<ImageTask> findByMember(Member creator, PageRequest pageRequest);

    Slice<ImageTask> findByMemberAndImageUrlIsNull(Member creator, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt);

    ImageTask save(ImageTask imageTask);

    Slice<ImageTask> findCreatedAfterAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable);
    Slice<ImageTask> findCreatedBeforeAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable);

    Slice<ImageTask> findByMemberAndImageUrlIsNotNull(Member member, PageRequest pageRequest);
    Slice<ImageTask> findCreatedAfterAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable);
    Slice<ImageTask> findCreatedBeforeAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable);

    Optional<ImageTask> findById(Long taskId);

    void delete(ImageTask task);
}