package com.ll.demo03.domain.image.dto;

import com.ll.demo03.domain.image.entity.Image;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ImageResponseDto {
    private String url;
    private String prompt;
    private String ratio;
    private Boolean isBookmarked;

    public static ImageResponseDto of(Image image) {
        return new ImageResponseDto(
                image.getUrl(),
                image.getTask().getRawPrompt(),
                image.getTask().getRatio(),
                image.getIsBookmarked()
        );
    }
}
