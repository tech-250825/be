package com.ll.demo03.domain.adminImage.dto;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.hashtag.entity.Hashtag;
import lombok.Getter;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class AdminImageResponse {
    private Long id;
    private String url;
    private String prompt;
    private Long mainCategoryId;
    private Long subCategoryId;
    private List<String> hashtags;

    public static AdminImageResponse from(AdminImage adminImage) {
        List<String> hashtags = adminImage.getHashtags()
                .stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());

        return AdminImageResponse.builder()
                .id(adminImage.getId())
                .url(adminImage.getUrl())
                .prompt(adminImage.getPrompt())
                .mainCategoryId(adminImage.getCategory().getParent().getId())
                .subCategoryId(adminImage.getCategory().getId())
                .hashtags(hashtags)
                .build();
    }
}
