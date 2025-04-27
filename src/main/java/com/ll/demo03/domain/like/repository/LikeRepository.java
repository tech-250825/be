package com.ll.demo03.domain.like.repository;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like, Long>, JpaSpecificationExecutor<Like> {

    Optional<Like> findByMemberAndImageId(Member member, Long imageId);
    boolean existsByMemberIdAndImageId(Long memberId, Long imageId);
    Slice<Like> findByMember(Member member, Specification<Like> specification, Pageable pageable);

    @Query("SELECT l.image.id FROM Like l WHERE l.image.id IN :imageIds AND l.member.id = :memberId")
    Set<Long> findImageIdsByImageIdInAndMemberId(List<Long> imageIds, Long memberId);

    @Query("SELECT l.image.id FROM Like l WHERE l.member.id = :memberId AND l.image.id IN :imageIds")
    List<Long> findLikedImageIdsByMemberIdAndImageIds(
            @Param("memberId") Long memberId,
            @Param("imageIds") List<Long> imageIds
    );

    boolean existsByImageIdLessThan(Long id);

    boolean existsByImageIdLessThanAndMemberId(Long id, Long id1);
}
