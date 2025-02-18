package com.ll.demo03.domain.image.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class ImageRequest {
    private String prompt;
    private String ratio;
}
