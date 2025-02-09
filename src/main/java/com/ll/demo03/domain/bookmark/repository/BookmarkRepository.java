package com.ll.demo03.domain.bookmark.repository;

import com.ll.demo03.domain.bookmark.entity.Bookmark;
import com.ll.demo03.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndImageId(Member member, Long imageId);

    List<Bookmark> findByMember(Member member);
}
