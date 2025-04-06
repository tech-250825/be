package com.ll.demo03.domain.like.repository;

import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByMemberAndImageId(Member member, Long imageId);
    boolean existsByMemberIdAndImageId(Long memberId, Long imageId);
    List<Like> findByMember(Member member);

    @Query("SELECT l.image.id FROM Like l WHERE l.image.id IN :imageIds AND l.member.id = :memberId")
    Set<Long> findImageIdsByImageIdInAndMemberId(List<Long> imageIds, Long memberId);

    @Query("SELECT l.image.id FROM Like l WHERE l.member.id = :memberId AND l.image.id IN :imageIds")
    List<Long> findLikedImageIdsByMemberIdAndImageIds(
            @Param("memberId") Long memberId,
            @Param("imageIds") List<Long> imageIds
    );
}
