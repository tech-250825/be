package com.ll.demo03.domain.sharedImage.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SharedImageResponseDto{
    private String nickname;
    private Long id;
    private String url;
    private String prompt;
    private String ratio;
    private int likeCount;

    public static SharedImageResponseDto of(SharedImage sharedImage) {
        Image image = sharedImage.getImage();
        return new SharedImageResponseDto(
                image.getMember().getNickname(),
                sharedImage.getId(),
                image.getUrl(),
                image.getTask().getRawPrompt(),
                image.getTask().getRatio(),
                sharedImage.getLikeCount()
        );
    }
}