package com.ll.demo03.domain.mypage.service;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.dto.PublicMemberDto;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public PageResponse<List<ImageResponse>> getMyImages(Member member, CursorBasedPageable pageable, String type) {
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());
        Slice<Image> imageSlice;

        boolean isVideoOnly = "video".equalsIgnoreCase(type);
        boolean isImageOnly = "image".equalsIgnoreCase(type);

        // 1. 페이지네이션 방향에 따라 쿼리 분기
        if (!pageable.hasCursors()) {
            // 첫 페이지
            if (isVideoOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNotNullOrderByIdDesc(member.getId(), pageRequest);
            } else if (isImageOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNullOrderByIdDesc(member.getId(), pageRequest);
            } else {
                imageSlice = imageRepository.findByMemberIdOrderByIdDesc(member.getId(), pageRequest);
            }
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));

            if (isVideoOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNotNullAndIdGreaterThanOrderByIdAsc(
                        member.getId(), cursorId, pageRequest);
            } else if (isImageOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNullAndIdGreaterThanOrderByIdAsc(
                        member.getId(), cursorId, pageRequest);
            } else {
                imageSlice = imageRepository.findByMemberIdAndIdGreaterThanOrderByIdAsc(
                        member.getId(), cursorId, pageRequest);
            }

            // 결과 역순 정렬
            List<Image> content = new ArrayList<>(imageSlice.getContent());
            Collections.reverse(content);
            imageSlice = new SliceImpl<>(content, pageRequest, imageSlice.hasNext());

        } else {
            // 다음 페이지 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));

            if (isVideoOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNotNullAndIdLessThanOrderByIdDesc(
                        member.getId(), cursorId, pageRequest);
            } else if (isImageOnly) {
                imageSlice = imageRepository.findByMemberIdAndVideoTaskIsNullAndIdLessThanOrderByIdDesc(
                        member.getId(), cursorId, pageRequest);
            } else {
                imageSlice = imageRepository.findByMemberIdAndIdLessThanOrderByIdDesc(
                        member.getId(), cursorId, pageRequest);
            }
        }

        // 2. 비어있는 경우 처리
        if (!imageSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Image> images = imageSlice.getContent();
        Image firstImage = images.get(0);
        Image lastImage = images.get(images.size() - 1);

        // 3. 좋아요 정보 조회
        Set<Long> likedImageIds = fetchLikedImageIds(member.getId(), images);

        // 4. 응답 변환
        List<ImageResponse> imageResponses = convertToImageResponses(images, likedImageIds);

        // 5. 커서 여부 확인 (type 조건 반영)
        boolean hasPrev, hasNext;

        if (isVideoOnly) {
            hasPrev = imageRepository.existsByIdGreaterThanAndMemberIdAndVideoTaskIsNotNull(firstImage.getId(), member.getId());
            hasNext = imageRepository.existsByIdLessThanAndMemberIdAndVideoTaskIsNotNull(lastImage.getId(), member.getId());
        } else if (isImageOnly) {
            hasPrev = imageRepository.existsByIdGreaterThanAndMemberIdAndVideoTaskIsNull(firstImage.getId(), member.getId());
            hasNext = imageRepository.existsByIdLessThanAndMemberIdAndVideoTaskIsNull(lastImage.getId(), member.getId());
        } else {
            hasPrev = imageRepository.existsByIdGreaterThanAndMemberId(firstImage.getId(), member.getId());
            hasNext = imageRepository.existsByIdLessThanAndMemberId(lastImage.getId(), member.getId());
        }

        String prevCursor = pageable.getEncodedCursor(String.valueOf(firstImage.getId()), hasPrev);
        String nextCursor = pageable.getEncodedCursor(String.valueOf(lastImage.getId()), hasNext);

        return new PageResponse<>(imageResponses, prevCursor, nextCursor);
    }


    // 분리된 헬퍼 메서드
    private Set<Long> fetchLikedImageIds(Long memberId, List<Image> images) {
        if (images.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> imageIds = images.stream()
                .map(Image::getId)
                .toList();

        return likeRepository.findImageIdsByImageIdInAndMemberId(imageIds, memberId);
    }

    private List<ImageResponse> convertToImageResponses(List<Image> images, Set<Long> likedImageIds) {
        return images.stream()
                .map(image -> ImageResponse.of(image, likedImageIds.contains(image.getId())))
                .toList();
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