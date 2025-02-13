package com.ll.demo03.domain.imageCategory.service;

import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.domain.imageCategory.repository.CategoryRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public ImageCategory findSubCategory(Long mainCategoryId, Long subCategoryId) {
        // 1. 메인 카테고리 찾기
        ImageCategory mainCategory = categoryRepository.findById(mainCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        // 2. 메인 카테고리가 진짜 메인 카테고리인지 확인 (부모가 없어야 함)
        if (mainCategory.getParent() != null) {
            throw new IllegalArgumentException(
                    "Category " + mainCategoryId + " is not a main category");
        }

        // 3. 서브 카테고리 찾기
        ImageCategory subCategory = categoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        // 4. 서브 카테고리가 해당 메인 카테고리에 속하는지 확인
        if (!mainCategory.equals(subCategory.getParent())) {
            throw new IllegalArgumentException(
                    "Sub category " + subCategoryId +
                            " does not belong to main category " + mainCategoryId);
        }

        return subCategory;
    }
}
