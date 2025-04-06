package com.ll.demo03.domain.mypage.folder.dto;

import com.ll.demo03.domain.mypage.folder.entity.Folder;
import com.ll.demo03.domain.image.dto.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class FolderResponse {
    private String name;
    private ImageResponse coverImage;

    public static FolderResponse of(Folder folder) {
        return FolderResponse.builder()
                .name(folder.getName())
                .coverImage(
                        Optional.ofNullable(folder.getImages())
                                .filter(images -> !images.isEmpty())
                                .map(images -> ImageResponse.of(images.get(0)))
                                .orElse(null)
                )
                .build();
    }

}