package com.ll.demo03.domain.adminImage.repository;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminImageRepository extends JpaRepository<AdminImage, Long> {
    void deleteById(Long adminId);
    Optional<AdminImage> findById(Long adminId);
    List<AdminImage> findByCategoryParentId(Long mainCategoryId);  // 메인 카테고리로 조회
    List<AdminImage> findByCategoryId(Long subCategoryId);         // 서브 카테고리로 조회
}
