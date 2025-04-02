package com.ll.demo03.domain.image.dto;

import com.ll.demo03.domain.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ImageResponseDto {
    private Long id;
    private String url;
    private String prompt;
    private String ratio;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;

    public static ImageResponseDto of(Image image) {
        return new ImageResponseDto(
                image.getId(),
                image.getUrl(),
                image.getTask().getRawPrompt(),
                image.getTask().getRatio(),
                image.getIsBookmarked(),
                image.getCreatedAt()
        );
    }
}
