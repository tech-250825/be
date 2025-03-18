package com.ll.demo03.domain.sharedImage.repository;

import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedImageRepository extends JpaRepository<SharedImage, Long> {
    SharedImage findByImageId(Long imageId);

    List<SharedImage> findAllByImage_Member_Id(Long memberId);
}
