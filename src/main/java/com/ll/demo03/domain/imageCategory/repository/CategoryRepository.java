package com.ll.demo03.domain.imageCategory.repository;

import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<ImageCategory, Long> {
    void deleteById(Long categoryId);
    Optional<ImageCategory> findById(Long categoryId);
}
