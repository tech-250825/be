package com.ll.demo03.domain.like.repository;

import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByMemberAndImageId(Member member, Long imageId);
    boolean existsByMemberIdAndImageId(Long memberId, Long imageId);
    List<Like> findByMember(Member member);
}
