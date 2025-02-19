package com.ll.demo03.domain.image.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.imageGenerate.entity.ImageGenerate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ImageResponseDto {
    private Long id;
    private String prompt;
    private String ratio;
    private String imageUrl;  // 단일 이미지 URL
    private LocalDateTime createdAt;

    @Builder
    public ImageResponseDto(Image image) {
        ImageGenerate imageGenerate = image.getImageGenerate();
        this.id = image.getId();
        this.prompt = imageGenerate.getPrompt();
        this.ratio = imageGenerate.getRatio();
        this.imageUrl = image.getUrl();
        this.createdAt = imageGenerate.getCreatedAt();
    }
}