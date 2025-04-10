package com.ll.demo03.domain.mypage.folder.dto;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.mypage.folder.entity.Folder;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class FolderResponse {
    private long id;
    private String name;
    private int imageCount;
    private int videoCount;
    private String coverImage;
    private String modifiedAt;

    public static FolderResponse of(Folder folder) {
        List<Image> images = folder.getImages();

        int imgCount = 0;
        int vidCount = 0;

        if (images != null) {
            imgCount = (int) images.stream()
                    .filter(img -> img.getVideoTask() == null)
                    .count();

            vidCount = (int) images.stream()
                    .filter(img -> img.getVideoTask() != null)
                    .count();
        }

        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .imageCount(imgCount)
                .videoCount(vidCount)
                .coverImage(
                        Optional.ofNullable(images)
                                .filter(imgs -> !imgs.isEmpty())
                                .map(imgs -> imgs.get(0).getUrl())
                                .orElse(null)
                )
                .modifiedAt(folder.getModifiedAt().toString())
                .build();
    }
}