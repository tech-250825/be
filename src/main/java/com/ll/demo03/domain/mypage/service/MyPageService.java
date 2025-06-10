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
    public PageResponse<List<ImageResponse>> getMyImages(Member member, CursorBasedPageable pageable) {
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());
        Slice<Image> imageSlice;

        // 1. 커서 방향에 따른 처리 개선
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            imageSlice = imageRepository.findByMemberIdOrderByIdDesc(member.getId(), pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            log.info("Moving to previous page, cursorId: {}", cursorId);
            imageSlice = imageRepository.findByMemberIdAndIdGreaterThanOrderByIdAsc(
                    member.getId(), cursorId, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 내림차순 표시 유지
            List<Image> content = new ArrayList<>(imageSlice.getContent());
            Collections.reverse(content);
            imageSlice = new SliceImpl<>(content, pageRequest, imageSlice.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            log.info("Moving to next page, cursorId: {}", cursorId);
            imageSlice = imageRepository.findByMemberIdAndIdLessThanOrderByIdDesc(
                    member.getId(), cursorId, pageRequest);
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!imageSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Image> images = imageSlice.getContent();
        Image firstImage = images.get(0);
        Image lastImage = images.get(images.size() - 1);

        // 3. 좋아요 정보 조회 최적화
        Set<Long> likedImageIds = fetchLikedImageIds(member.getId(), images);

        // 4. 응답 변환 로직 분리
        List<ImageResponse> imageResponses = convertToImageResponses(images, likedImageIds);

        // 5. 이전/다음 페이지 커서 생성 개선
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstImage.getId()),
                imageRepository.existsByIdGreaterThanAndMemberId(firstImage.getId(), member.getId())
        );

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastImage.getId()),
                imageRepository.existsByIdLessThanAndMemberId(lastImage.getId(), member.getId())
        );

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
                .map(image -> new ImageResponse(
                        PublicMemberDto.of(image.getMember()),
                        image.getId(),
                        image.getUrl(),
                        image.getTask().getRawPrompt(),
                        image.getTask().getRatio(),
                        image.getLikeCount(),
                        likedImageIds.contains(image.getId()),
                        image.getIsShared(),
                        image.getUpscaleTask() != null,
                        image.getTask().getTaskId(),
                        image.getImgIndex(),
                        image.getCreatedAt()
                ))
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