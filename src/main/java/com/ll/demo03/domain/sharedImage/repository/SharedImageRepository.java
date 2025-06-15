package com.ll.demo03.domain.sharedImage.repository;

import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface SharedImageRepository extends JpaRepository<SharedImage, Long> , JpaSpecificationExecutor<SharedImage> {
    SharedImage findByImageId(Long imageId);

    Optional<SharedImage> findByImage_Id(Long id);
}
