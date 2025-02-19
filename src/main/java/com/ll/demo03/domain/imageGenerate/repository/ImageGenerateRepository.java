package com.ll.demo03.domain.imageGenerate.repository;

import com.ll.demo03.domain.imageGenerate.entity.ImageGenerate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageGenerateRepository extends JpaRepository<ImageGenerate,Long> {
    Optional<ImageGenerate> findByTaskid(String taskId);
}
