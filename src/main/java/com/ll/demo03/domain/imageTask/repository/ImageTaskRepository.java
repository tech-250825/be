package com.ll.demo03.domain.imageTask.repository;

import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface ImageTaskRepository extends JpaRepository<ImageTask, Long>, JpaSpecificationExecutor<ImageTask> {

    void deleteByMemberId(Long memberId);

    Slice<ImageTask> findByMember(Member member, Pageable pageable);

    boolean existsByMemberAndCreatedAtGreaterThan(Member member, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(Member member, LocalDateTime createdAt);
}
