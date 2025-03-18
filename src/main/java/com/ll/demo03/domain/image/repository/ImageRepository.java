package com.ll.demo03.domain.image.repository;

import com.ll.demo03.domain.image.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findByMemberIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    Page<Image> findByBookmarkedOrderByCreatedAtDesc(boolean b, Pageable pageable);
}
