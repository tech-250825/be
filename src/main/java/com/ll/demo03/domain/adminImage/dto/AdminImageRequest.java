package com.ll.demo03.domain.adminImage.dto;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class AdminImageRequest {
    private String url;
    private String prompt;
    private Long mainCategoryId;
    private Long subCategoryId;
}

