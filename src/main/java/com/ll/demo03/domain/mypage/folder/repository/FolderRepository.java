package com.ll.demo03.domain.mypage.folder.repository;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.folder.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Page<Folder> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);
}
