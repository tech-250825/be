package com.ll.demo03.domain.adminImage.dto;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminImageRequest {
    private String url;
    private String prompt;
    private List<String> hashtags;
    private Long mainCategoryId;
    private Long subCategoryId;

    public AdminImage toEntity(CategoryService categoryService) {
        ImageCategory category = categoryService.findSubCategory(mainCategoryId, subCategoryId);

        return AdminImage.builder()
                .url(url)
                .prompt(prompt)
                .category(category)  // ImageCategory 객체 전달
                .build();
    }
}


