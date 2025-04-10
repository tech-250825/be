package com.ll.demo03.domain.sharedImage.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponse;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly=true)
public class SharedImageService {

    private final SharedImageRepository sharedImageRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    public Page<SharedImageResponse> getAllSharedImageResponses(Long memberId, Pageable pageable) {
        Page<SharedImage> sharedImagesPage = sharedImageRepository.findAll(pageable);

        List<Long> imageIds = sharedImagesPage.getContent().stream()
                .map(sharedImage -> sharedImage.getImage().getId())
                .collect(Collectors.toList());

        Set<Long> likedImageIds = memberId != null
                ? new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, imageIds))
                : Collections.emptySet();

        return sharedImagesPage.map(sharedImage -> {
            Long imageId = sharedImage.getImage().getId();
            Boolean isLiked = memberId != null
                    ? likedImageIds.contains(imageId)
                    : null;
            return SharedImageResponse.of(sharedImage, isLiked);
        });
    }

    public Page<SharedImageResponse> getMySharedImages(Long memberId, Pageable pageable) {
        Page<SharedImage> sharedImagesPage = sharedImageRepository.findAllByImage_Member_Id(memberId, pageable);
        List<Long> imageIds = sharedImagesPage.getContent().stream()
                .map(sharedImage -> sharedImage.getImage().getId())
                .collect(Collectors.toList());

        Set<Long> likedImageIds = memberId != null
                ? new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, imageIds))
                : Collections.emptySet();

        return sharedImagesPage.map(sharedImage -> {
            Long imageId = sharedImage.getImage().getId();
            boolean isLiked = likedImageIds.contains(imageId);
            return SharedImageResponse.of(sharedImage, isLiked);
        });
    }

    @Transactional
    public SharedImage createSharedImage(Long imageId) {

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SharedImage existingShare = sharedImageRepository.findByImageId(imageId);
        if (existingShare != null) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        SharedImage sharedImage = new SharedImage();
        sharedImage.setImage(image);

        SharedImage saved = sharedImageRepository.save(sharedImage);

        return saved;
    }


    @Transactional
    public boolean deleteSharedImage(Long id, Long userId) {

        SharedImage sharedImage = sharedImageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!sharedImage.getImage().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        sharedImageRepository.delete(sharedImage);
        return true;
    }

}