package com.ll.demo03.domain.sharedImage.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.dto.PublicMemberDto;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SharedImageResponse {
    private PublicMemberDto member;
    private Long id;
    private String url;
    private String prompt;
    private String ratio;
    private int likeCount;
    private Boolean isLiked;
    private String createdAt;
    private String prevCursor;
    private String nextCursor;

    public static SharedImageResponse of(SharedImage sharedImage, String prevCursor, String nextCursor) {
        return of(sharedImage, null, prevCursor, nextCursor);
    }

    public static SharedImageResponse of(SharedImage sharedImage, Boolean isLiked, String prevCursor, String nextCursor) {
        Image image = sharedImage.getImage();

        String rawPrompt = null;
        String ratio = null;

        if (image.getImageTask() != null) {
            rawPrompt = image.getImageTask().getRawPrompt();
            ratio = image.getImageTask().getRatio();
        }

        return new SharedImageResponse(
                PublicMemberDto.of(image.getMember()),
                sharedImage.getId(),
                image.getUrl(),
                rawPrompt,
                ratio,
                image.getLikeCount(),
                isLiked,
                image.getCreatedAt().toString(),
                prevCursor,
                nextCursor
        );
    }
}
