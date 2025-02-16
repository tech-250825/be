package com.ll.demo03.domain.adminImage.service;

import com.ll.demo03.domain.adminImage.dto.AdminImageRequest;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
import com.ll.demo03.domain.hashtag.service.HashtagService;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
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

    public List<AdminImage> findAll() {
        return adminImageRepository.findAll();
    }

    public void delete(Long id) {
        adminImageRepository.deleteById(id);
    }

    public List<AdminImage> findByMainCategory(Long mainCategoryId) {
        return adminImageRepository.findByCategoryParentId(mainCategoryId);
    }

    public List<AdminImage> findBySubCategory(Long subCategoryId) {
        return adminImageRepository.findByCategoryId(subCategoryId);
    }
}