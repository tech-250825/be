package com.ll.demo03.domain.sharedImage.repository;

import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface SharedImageRepository extends JpaRepository<SharedImage, Long> , JpaSpecificationExecutor<SharedImage> {
    SharedImage findByImageId(Long imageId);

    Slice<SharedImage> findAllByImage_Member_Id(Long memberId, Specification<SharedImage> specification, Pageable pageable);

    boolean existsByIdLessThan(Long id);

    boolean existsByIdGreaterThan(Long id);

    boolean existsByIdGreaterThanAndImage_Member_Id(Long id, Long memberId);

    boolean existsByIdLessThanAndImage_Member_Id(Long id, Long memberId);

    Optional<Object> findByImage_Id(Long id);
}
