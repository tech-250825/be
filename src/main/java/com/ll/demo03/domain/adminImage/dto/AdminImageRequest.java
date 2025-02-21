package com.ll.demo03.domain.adminImage.dto;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.hashtag.entity.Hashtag;
import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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

    public AdminImage toEntity(CategoryService categoryService, String url) {
        // 카테고리 찾기
        ImageCategory category = categoryService.findSubCategory(mainCategoryId, subCategoryId);

//        // List<String> -> List<Hashtag>로 변환하면서 각 Hashtag의 adminImage를 설정
//        List<Hashtag> hashtagList = this.hashtags.stream()
//                .map(hashtagName -> {
//                    Hashtag hashtag = Hashtag.create(hashtagName, null); // 아직 adminImage는 null로 설정
//                    hashtag.setAdminImage(null); // null로 설정해두고, 나중에 AdminImage로 설정
//                    return hashtag;
//                })
//                .collect(Collectors.toList());

        // AdminImage 객체 생성
        AdminImage adminImage = AdminImage.builder()
                .url(url)
                .prompt(prompt)
                .category(category)  // ImageCategory 객체 전달
                .build();

//        // AdminImage 객체가 생성되면 각 Hashtag 객체의 adminImage를 설정
//        hashtagList.forEach(hashtag -> hashtag.setAdminImage(adminImage));

        return adminImage;
    }
}


