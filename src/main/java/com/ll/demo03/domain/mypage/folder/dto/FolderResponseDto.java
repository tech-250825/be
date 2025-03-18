package com.ll.demo03.domain.mypage.folder.dto;

import com.ll.demo03.domain.mypage.folder.entity.Folder;
import com.ll.demo03.domain.image.dto.ImageResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class FolderResponseDto {
    private String name;
    private ImageResponseDto coverImage;

    public static FolderResponseDto of(Folder folder) {
        return FolderResponseDto.builder()
                .name(folder.getName())
                .coverImage(
                        Optional.ofNullable(folder.getImages())
                                .filter(images -> !images.isEmpty())
                                .map(images -> ImageResponseDto.of(images.get(0)))
                                .orElse(null)
                )
                .build();
    }

}