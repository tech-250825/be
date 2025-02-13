package com.ll.demo03.domain.adminImage.dto;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import lombok.Getter;
import lombok.Builder;

@Builder
@Getter
public class AdminImageResponse {
    private Long id;
    private String url;
    private String prompt;
    private Long mainCategoryId;
    private Long subCategoryId;

    public static AdminImageResponse from(AdminImage adminImage) {
        return AdminImageResponse.builder()
                .id(adminImage.getId())
                .url(adminImage.getUrl())
                .prompt(adminImage.getPrompt())
                .mainCategoryId(adminImage.getCategory().getParent().getId())
                .subCategoryId(adminImage.getCategory().getId())
                .build();
    }
}
