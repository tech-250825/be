package com.ll.demo03.domain.sharedImage.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponse;
import com.ll.demo03.domain.sharedImage.dto.SharedImagesResponse;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Service
@AllArgsConstructor
@Transactional(readOnly=true)
public class SharedImageService {

    private final SharedImageRepository sharedImageRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public PageResponse<List<SharedImagesResponse>> getAllSharedImageResponses(
            Long memberId,
            Specification<SharedImage> specification,
            CursorBasedPageable pageable
    ) {
        Slice<SharedImage> sharedImagesPage;

        Sort sort = pageable.hasPrevPageCursor()
                ? Sort.by(Sort.Direction.ASC, "id")
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable pageRequest = PageRequest.of(0, pageable.getSize(), sort);

        if (!pageable.hasCursors()) {
            sharedImagesPage = sharedImageRepository.findAll(specification, pageRequest);
        } else {
            Long cursorId = Long.parseLong(
                    pageable.hasPrevPageCursor()
                            ? pageable.getDecodedCursor(pageable.getPrevPageCursor())
                            : pageable.getDecodedCursor(pageable.getNextPageCursor())
            );

            Specification<SharedImage> cursorSpec = (root, query, cb) -> {
                Predicate base = specification.toPredicate(root, query, cb);
                Predicate cursorPredicate = pageable.hasPrevPageCursor()
                        ? cb.greaterThan(root.get("id"), cursorId)
                        : cb.lessThan(root.get("id"), cursorId);
                return cb.and(base, cursorPredicate);
            };

            sharedImagesPage = sharedImageRepository.findAll(cursorSpec, pageRequest);
            if (pageable.hasPrevPageCursor()) {
                List<SharedImage> reversed = new ArrayList<>(sharedImagesPage.getContent());
                Collections.reverse(reversed);
                sharedImagesPage = new SliceImpl<>(reversed, pageRequest, sharedImagesPage.hasNext());
            }
        }

        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        SharedImage first = sharedImages.get(0);
        SharedImage last = sharedImages.get(sharedImages.size() - 1);

        List<SharedImagesResponse> responseList = prepareSharedImageResponses(memberId, sharedImages);

        // 커서 전/후 존재 여부 판단은 필터링 포함하여 판단
        Specification<SharedImage> prevSpec = (root, query, cb) ->
                cb.and(specification.toPredicate(root, query, cb), cb.greaterThan(root.get("id"), first.getId()));
        boolean hasPrev = sharedImageRepository.count(prevSpec) > 0;

        Specification<SharedImage> nextSpec = (root, query, cb) ->
                cb.and(specification.toPredicate(root, query, cb), cb.lessThan(root.get("id"), last.getId()));
        boolean hasNext = sharedImageRepository.count(nextSpec) > 0;

        String prevCursor = pageable.getEncodedCursor(String.valueOf(first.getId()), hasPrev);
        String nextCursor = pageable.getEncodedCursor(String.valueOf(last.getId()), hasNext);

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }


    public PageResponse<List<SharedImagesResponse>> getMySharedImages(Long memberId, Specification<SharedImage> specification, CursorBasedPageable pageable) {
        Slice<SharedImage> sharedImagesPage;

        // 기본 memberId 조건을 Specification으로 만들기
        Specification<SharedImage> memberSpec = (root, query, cb) ->
                cb.equal(root.get("image").get("member").get("id"), memberId);

        // 기본 specification과 memberId 조건 결합 (null 체크 추가)
        Specification<SharedImage> baseSpec = specification != null ?
                memberSpec.and(specification) : memberSpec;

        // 1. 커서 방향에 따른 처리 개선
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 - 기본 정렬(ID 내림차순) 적용
            Sort defaultSort = Sort.by(Sort.Direction.DESC, "id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), defaultSort);
            sharedImagesPage = sharedImageRepository.findAll(baseSpec, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));

            // 커서 조건을 별도 Specification으로 생성
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.greaterThan(root.get("id"), cursorId);

            // 모든 조건 결합
            Specification<SharedImage> finalSpec = baseSpec.and(cursorSpec);

            // ID 오름차순으로 조회
            Sort ascSort = Sort.by(Sort.Direction.ASC, "id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            sharedImagesPage = sharedImageRepository.findAll(finalSpec, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 정렬 유지
            List<SharedImage> content = new ArrayList<>(sharedImagesPage.getContent());
            Collections.reverse(content);
            sharedImagesPage = new SliceImpl<>(content, pageRequest, sharedImagesPage.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));

            // 커서 조건을 별도 Specification으로 생성
            Specification<SharedImage> cursorSpec = (root, query, cb) ->
                    cb.lessThan(root.get("id"), cursorId);

            // 모든 조건 결합
            Specification<SharedImage> finalSpec = baseSpec.and(cursorSpec);

            // ID 내림차순으로 조회 (기본 정렬과 일치)
            Sort descSort = Sort.by(Sort.Direction.DESC, "id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), descSort);
            sharedImagesPage = sharedImageRepository.findAll(finalSpec, pageRequest);
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!sharedImagesPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<SharedImage> sharedImages = sharedImagesPage.getContent();
        SharedImage firstImage = sharedImages.get(0);
        SharedImage lastImage = sharedImages.get(sharedImages.size() - 1);

        // 3. 응답 데이터 준비
        List<SharedImagesResponse> responseList = prepareSharedImageResponses(memberId, sharedImages);

        // 4. 이전/다음 페이지 커서 생성
        Specification<SharedImage> prevPageSpec = baseSpec.and((root, query, cb) ->
                cb.greaterThan(root.get("id"), firstImage.getId()));

        boolean hasPrevPage = sharedImageRepository.count(prevPageSpec) > 0;

        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstImage.getId()),
                hasPrevPage
        );

        // 다음 페이지 확인
        Specification<SharedImage> nextPageSpec = baseSpec.and((root, query, cb) ->
                cb.lessThan(root.get("id"), lastImage.getId()));

        boolean hasNextPage = sharedImageRepository.count(nextPageSpec) > 0;

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastImage.getId()),
                hasNextPage
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }

    // 공통 로직을 분리한 헬퍼 메서드
    private List<SharedImagesResponse> prepareSharedImageResponses(Long memberId, List<SharedImage> sharedImages) {
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
                    return SharedImagesResponse.of(sharedImage, isLiked);
                })
                .toList();
    }


    public SharedImageResponse getSharedImage(Long memberId,
                                              Specification<SharedImage> specification,
                                              CursorBasedPageable pageable,
                                              Long imageId) {
        SharedImage sharedImage = sharedImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Boolean isLiked = null;
        if (memberId != null) {
            Set<Long> likedImageIds = new HashSet<>(likeRepository.findLikedImageIdsByMemberIdAndImageIds(memberId, List.of(imageId)));
            isLiked = likedImageIds.contains(imageId);
        }

        Specification<SharedImage> prevSpec = (root, query, cb) ->
                cb.and(specification.toPredicate(root, query, cb), cb.greaterThan(root.get("id"), imageId));
        boolean hasPrev = sharedImageRepository.count(prevSpec) > 0;

        Specification<SharedImage> nextSpec = (root, query, cb) ->
                cb.and(specification.toPredicate(root, query, cb), cb.lessThan(root.get("id"), imageId));
        boolean hasNext = sharedImageRepository.count(nextSpec) > 0;

        String prevCursor = pageable.getEncodedCursor(String.valueOf(imageId), hasPrev);
        String nextCursor = pageable.getEncodedCursor(String.valueOf(imageId), hasNext);

        return SharedImageResponse.of(sharedImage, isLiked, prevCursor, nextCursor);
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
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!sharedImage.getImage().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        image.setIsShared(false);

        imageRepository.save(image);
        sharedImageRepository.delete(sharedImage);

        return true;
    }

}