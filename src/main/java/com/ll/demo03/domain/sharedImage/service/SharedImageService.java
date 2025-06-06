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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
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
        Slice<SharedImage> sharedImagesPage;

        // 1. 커서 방향에 따른 처리 개선
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            sharedImagesPage = sharedImageRepository.findAll(specification, ofSize(pageable.getSize()));
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            // 역방향 스펙 생성 (ID가 더 큰 항목)
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.and(specification.toPredicate(root, query, cb),
                            cb.greaterThan(root.get("id"), cursorId));

            // ID 오름차순으로 조회
            Sort ascSort = Sort.by(Sort.Direction.ASC, "id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            sharedImagesPage = sharedImageRepository.findAll(cursorSpec, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 정렬 유지
            List<SharedImage> content = new ArrayList<>(sharedImagesPage.getContent());
            Collections.reverse(content);
            sharedImagesPage = new SliceImpl<>(content, pageRequest, sharedImagesPage.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            // 다음 페이지 스펙 (ID가 더 작은 항목)
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.and(specification.toPredicate(root, query, cb),
                            cb.lessThan(root.get("id"), cursorId));

            sharedImagesPage = sharedImageRepository.findAll(cursorSpec, ofSize(pageable.getSize()));
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        SharedImage firstImage = sharedImages.get(0);
        SharedImage lastImage = sharedImages.get(sharedImages.size() - 1);

        // 3. 응답 데이터 준비
        List<SharedImageResponse> responseList = prepareSharedImageResponses(memberId, sharedImages);

        // 4. 이전/다음 페이지 커서 생성
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstImage.getId()),
                sharedImageRepository.existsByIdGreaterThan(firstImage.getId())
        );

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastImage.getId()),
                sharedImageRepository.existsByIdLessThan(lastImage.getId())
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }

    public PageResponse<List<SharedImageResponse>> getMySharedImages(Long memberId, Specification<SharedImage> specification, CursorBasedPageable pageable) {
        Slice<SharedImage> sharedImagesPage;

        // 1. 커서 방향에 따른 처리 개선
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            sharedImagesPage = sharedImageRepository.findAllByImage_Member_Id(memberId, specification, ofSize(pageable.getSize()));
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
            // 역방향 스펙 생성 (ID가 더 큰 항목)
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.and(specification.toPredicate(root, query, cb),
                            cb.greaterThan(root.get("id"), cursorId));

            // ID 오름차순으로 조회
            Sort ascSort = Sort.by(Sort.Direction.ASC, "id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            sharedImagesPage = sharedImageRepository.findAllByImage_Member_Id(memberId, cursorSpec, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 정렬 유지
            List<SharedImage> content = new ArrayList<>(sharedImagesPage.getContent());
            Collections.reverse(content);
            sharedImagesPage = new SliceImpl<>(content, pageRequest, sharedImagesPage.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            // 다음 페이지 스펙 (ID가 더 작은 항목)
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.and(specification.toPredicate(root, query, cb),
                            cb.lessThan(root.get("id"), cursorId));

            sharedImagesPage = sharedImageRepository.findAllByImage_Member_Id(memberId, cursorSpec, ofSize(pageable.getSize()));
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        SharedImage firstImage = sharedImages.get(0);
        SharedImage lastImage = sharedImages.get(sharedImages.size() - 1);

        // 3. 응답 데이터 준비 - 여기서는 항상 memberId가 null이 아님
        List<SharedImageResponse> responseList = prepareSharedImageResponses(memberId, sharedImages);

        // 4. 이전/다음 페이지 커서 생성
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstImage.getId()),
                sharedImageRepository.existsByIdGreaterThanAndImage_Member_Id(firstImage.getId(), memberId)
        );

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastImage.getId()),
                sharedImageRepository.existsByIdLessThanAndImage_Member_Id(lastImage.getId(), memberId)
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }

    // 공통 로직을 분리한 헬퍼 메서드
    private List<SharedImageResponse> prepareSharedImageResponses(Long memberId, List<SharedImage> sharedImages) {
        if (sharedImages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> imageIds = sharedImages.stream()
                .map(sharedImage -> sharedImage.getImage().getId())
                .toList();

        Set<Long> likedImageIds = memberId != null
                ? new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, imageIds))
                : Collections.emptySet();

        return sharedImages.stream()
                .map(sharedImage -> {
                    Long imageId = sharedImage.getImage().getId();
                    Boolean isLiked = memberId != null ? likedImageIds.contains(imageId) : null;
                    return SharedImageResponse.of(sharedImage, isLiked);
                })
                .toList();
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

        SharedImage sharedImage = sharedImageRepository.findByImage_Id(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!sharedImage.getImage().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        sharedImageRepository.delete(sharedImage);
        return true;
    }

}