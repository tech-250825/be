package com.ll.demo03.domain.folder.repository;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.folder.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>,JpaSpecificationExecutor<Folder> {

    boolean existsByMemberAndCreatedAtGreaterThan(Member member, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(Member member, LocalDateTime createdAt);

    Slice<Folder> findByMember(Member member, PageRequest pageRequest);

    void deleteByMemberId(Long memberId);
}
