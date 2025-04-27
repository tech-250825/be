package com.ll.demo03.domain.sharedImage.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponse;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.data.domain.PageRequest.ofSize;

import java.util.*;


@Service
@AllArgsConstructor
@Transactional(readOnly=true)
public class SharedImageService {

    private final SharedImageRepository sharedImageRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    public PageResponse<List<SharedImageResponse>> getAllSharedImageResponses(Long memberId, Specification<SharedImage> specification, CursorBasedPageable pageable) {
        Slice<SharedImage> sharedImagesPage = sharedImageRepository.findAll(specification, ofSize(pageable.getSize()));

        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        List<Long> imageIds = sharedImages.stream()
                .map(sharedImage -> sharedImage.getImage().getId())
                .toList();

        Set<Long> likedImageIds = memberId != null
                ? new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, imageIds))
                : Collections.emptySet();

        List<SharedImageResponse> responseList = sharedImages.stream()
                .map(sharedImage -> {
                    Long imageId = sharedImage.getImage().getId();
                    Boolean isLiked = memberId != null ? likedImageIds.contains(imageId) : null;
                    return SharedImageResponse.of(sharedImage, isLiked);
                })
                .toList();

        return new PageResponse<>(
                responseList,
                pageable.getEncodedCursor(
                        String.valueOf(sharedImages.get(0).getId()),
                        sharedImageRepository.existsByIdLessThan(sharedImages.get(0).getId())
                ),
                pageable.getEncodedCursor(
                        String.valueOf(sharedImages.get(sharedImages.size() - 1).getId()),
                        sharedImagesPage.hasNext()
                )
        );
    }


    public PageResponse<List<SharedImageResponse>> getMySharedImages(Long memberId, Specification<SharedImage> specification, CursorBasedPageable pageable) {
        Slice<SharedImage> sharedImagesPage = sharedImageRepository.findAllByImage_Member_Id(memberId, specification, ofSize(pageable.getSize()));

        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        List<Long> imageIds = sharedImages.stream()
                .map(sharedImage -> sharedImage.getImage().getId())
                .toList();

        Set<Long> likedImageIds = new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, imageIds));

        List<SharedImageResponse> responseList = sharedImages.stream()
                .map(sharedImage -> {
                    Long imageId = sharedImage.getImage().getId();
                    Boolean isLiked = likedImageIds.contains(imageId);
                    return SharedImageResponse.of(sharedImage, isLiked);
                })
                .toList();

        return new PageResponse<>(
                responseList,
                pageable.getEncodedCursor(
                        String.valueOf(sharedImages.get(0).getId()),
                        sharedImageRepository.existsByIdLessThan(sharedImages.get(0).getId())
                ),
                pageable.getEncodedCursor(
                        String.valueOf(sharedImages.get(sharedImages.size() - 1).getId()),
                        sharedImagesPage.hasNext()
                )
        );
    }



    public SharedImageResponse getSharedImage(Long memberId, Long imageId) {
        SharedImage sharedImage = sharedImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Boolean isLiked = null;
        if (memberId != null) {
            Set<Long> likedImageIds = new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, List.of(imageId)));
            isLiked = likedImageIds.contains(imageId);
        }

        return SharedImageResponse.of(sharedImage, isLiked);
    }


    @Transactional
    public SharedImage createSharedImage(Long imageId) {

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SharedImage existingShare = sharedImageRepository.findByImageId(imageId);
        if (existingShare != null) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        image.setIsShared(true);

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