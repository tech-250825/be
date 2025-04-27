package com.ll.demo03.domain.image.repository;

import com.ll.demo03.domain.image.entity.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ImageRepository extends JpaRepository<Image, Long>, JpaSpecificationExecutor<Image> {
    Slice<Image> findByMemberIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    Slice<Image> findByFolderId(Long folderId, Pageable pageable);

    boolean existsByIdLessThan(Long id);

}
