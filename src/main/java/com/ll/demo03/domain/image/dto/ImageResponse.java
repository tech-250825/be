package com.ll.demo03.domain.image.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.dto.PublicMemberDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ImageResponse {
    private PublicMemberDto member;
    private Long id;
    private String url;
    private String prompt;
    private String ratio;
    private int likeCount;
    private Boolean isLiked;
    private Boolean isShared;
    private Boolean isUpscaled;
    private String taskId;
    private int index;
    private LocalDateTime createdAt;

    // 좋아요 상태를 직접 받는 팩토리 메서드
    public static ImageResponse of(Image image, boolean isLiked) {
        return new ImageResponse(
                PublicMemberDto.of(image.getMember()),
                image.getId(),
                image.getUrl(),
                image.getTask().getRawPrompt(),
                image.getTask().getRatio(),
                image.getLikeCount(),
                isLiked,
               image.getUpscaleTask() != null || Boolean.TRUE.equals(image.getIsShared()),
                image.getIsUpscaled(),
                image.getTask().getTaskId(),
                image.getImgIndex(),
                image.getCreatedAt()
        );
    }

    // 기본 팩토리 메서드 (isLiked = false)
    public static ImageResponse of(Image image) {
        return of(image, false);
    }
}
