package com.ll.demo03.domain.sharedImage.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.dto.PublicMemberDto;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SharedImagesResponse {
    private PublicMemberDto member;
    private Long id;
    private String url;
    private String prompt;
    private String ratio;
    private int likeCount;
    private Boolean isLiked;
    private String createdAt;

    public static SharedImagesResponse of(SharedImage sharedImage) {
        return of(sharedImage, null);
    }

    public static SharedImagesResponse of(SharedImage sharedImage, Boolean isLiked) {
        Image image = sharedImage.getImage();

        String rawPrompt = null;
        String ratio = null;

        if (image.getImageTask() != null) {
            rawPrompt = image.getImageTask().getRawPrompt();
            ratio = image.getImageTask().getRatio();
        }

        return new SharedImagesResponse(
                PublicMemberDto.of(image.getMember()),
                sharedImage.getId(),
                image.getUrl(),
                rawPrompt,
                ratio,
                image.getLikeCount(),
                isLiked,
                image.getCreatedAt().toString()
        );
    }
}

