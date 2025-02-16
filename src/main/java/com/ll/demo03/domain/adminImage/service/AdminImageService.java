package com.ll.demo03.domain.adminImage.service;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
import com.ll.demo03.domain.hashtag.service.HashtagService;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminImageService {

    private final AdminImageRepository adminImageRepository;
    private final HashtagService hashtagService;
    private final CategoryService categoryService;

//    public AdminImage save(AdminImageRequest adminImageRequest) {
//        AdminImage adminImage = adminImageRequest.toEntity(categoryService);
//        AdminImage savedImage =adminImageRepository.save(adminImage);
//        hashtagService.createHashtags(savedImage, adminImageRequest.getHashtags());
//        return savedImage;
//    }

    public AdminImage findById(Long id) {
        return adminImageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public Page<AdminImage> findAll(Pageable pageable) {
        return adminImageRepository.findAll(pageable);
    }

    public void delete(Long id) {
        adminImageRepository.deleteById(id);
    }

    public Page<AdminImage> findByMainCategory(Long mainCategoryId, Pageable pageable) {
        return adminImageRepository.findByCategoryParentId(mainCategoryId, pageable);
    }

    public Page<AdminImage> findBySubCategory(Long subCategoryId,  Pageable pageable) {
        return adminImageRepository.findByCategoryId(subCategoryId, pageable);
    }
}