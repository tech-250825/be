package com.ll.demo03.domain.adminImage.repository;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminImageRepository extends JpaRepository<AdminImage, Long> {
    void deleteById(Long adminId);
    Optional<AdminImage> findById(Long adminId);
    Page<AdminImage> findByCategoryParentId(Long mainCategoryId, Pageable pageable);  // 메인 카테고리로 조회
    Page<AdminImage> findByCategoryId(Long subCategoryId, Pageable pageable);         // 서브 카테고리로 조회
}
