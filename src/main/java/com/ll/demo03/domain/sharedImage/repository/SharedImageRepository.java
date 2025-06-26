package com.ll.demo03.domain.sharedImage.repository;

import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SharedImageRepository extends JpaRepository<SharedImage, Long> , JpaSpecificationExecutor<SharedImage> {
    SharedImage findByImageId(Long imageId);

    Optional<SharedImage> findByImage_Id(Long id);
    @Query("SELECT COUNT(si) FROM SharedImage si WHERE si.image.member.id = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM SharedImage si WHERE si.image.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
