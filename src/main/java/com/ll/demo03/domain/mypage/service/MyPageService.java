package com.ll.demo03.domain.mypage.service;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.data.domain.PageRequest.ofSize;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public PageResponse<List<ImageResponse>> getMyImages(Member member, CursorBasedPageable pageable) {
        Slice<Image> imageSlice = imageRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(),ofSize(pageable.getSize()));

        if (!imageSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Image> images = imageSlice.getContent();
        List<Long> imageIds = images.stream()
                .map(Image::getId)
                .toList();

        Set<Long> likedImageIds = imageIds.isEmpty() ?
                Collections.emptySet() :
                likeRepository.findImageIdsByImageIdInAndMemberId(imageIds, member.getId());

        List<ImageResponse> imageResponses = images.stream()
                .map(image -> new ImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getTask().getRawPrompt(),
                        image.getTask().getRatio(),
                        image.getLikeCount(),
                        likedImageIds.contains(image.getId()),
                        image.getIsShared(),
                        image.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(
                imageResponses,
                pageable.getEncodedCursor(
                        String.valueOf(images.get(0).getId()),
                        imageRepository.existsByIdLessThan(images.get(0).getId())
                ),
                pageable.getEncodedCursor(
                        String.valueOf(images.get(images.size() - 1).getId()),
                        imageSlice.hasNext()
                )
        );
    }


    @Transactional
    public void deleteMyImages(ImageIdsRequest imageIds, Member member) {
        for (Long imageId : imageIds.getImageIds()) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

            if (!image.getMember().getId().equals(member.getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            try {
                imageRepository.delete(image);
            } catch (Exception e) {
                log.error("Error deleting image: ", e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

}