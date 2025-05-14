package com.ll.demo03.domain.like.service;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.CursorPagingUtils;
import com.ll.demo03.global.util.PageResponse;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


import static org.springframework.data.domain.PageRequest.ofSize;


@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public void addLike(Long imageId, Member member) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        boolean alreadyLiked = likeRepository.existsByMemberIdAndImageId(member.getId(), imageId);
        if (alreadyLiked) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        Like like = new Like();
        like.setMember(member);
        like.setImage(image);
        likeRepository.save(like);

        image.setLikeCount(image.getLikeCount() + 1);
        imageRepository.save(image);
    }

    @Transactional
    public void deleteLike(Long imageId, Member member) {

        Like like = likeRepository.findByMemberAndImageId(member, imageId)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likeRepository.delete(like);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        int newLikeCount = Math.max(0, image.getLikeCount() - 1);
        image.setLikeCount(newLikeCount);
        imageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public PageResponse<List<ImageResponse>> getMyLikes(Member member, CursorBasedPageable pageable) {
        Slice<Like> likeSlice;

        // 1. 커서 방향에 따른 처리 개선
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            likeSlice = likeRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("member"), member),
                    ofSize(pageable.getSize())
            );
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));

            // 역방향 스펙 생성 (ID가 더 큰 항목)
            Specification<Like> cursorSpec = (root, query, cb) ->
                    cb.and(
                            cb.equal(root.get("member"), member),
                            cb.greaterThan(root.get("image").get("id"), cursorId)
                    );

            // ID 오름차순으로 조회
            Sort ascSort = Sort.by(Sort.Direction.ASC, "image.id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            likeSlice = likeRepository.findAll(cursorSpec, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 정렬 유지
            List<Like> content = new ArrayList<>(likeSlice.getContent());
            Collections.reverse(content);
            likeSlice = new SliceImpl<>(content, pageRequest, likeSlice.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));

            // 다음 페이지 스펙 (ID가 더 작은 항목)
            Specification<Like> cursorSpec = (root, query, cb) ->
                    cb.and(
                            cb.equal(root.get("member"), member),
                            cb.lessThan(root.get("image").get("id"), cursorId)
                    );

            likeSlice = likeRepository.findAll(cursorSpec, ofSize(pageable.getSize()));
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!likeSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Like> likes = likeSlice.getContent();

        // 첫 번째와 마지막 항목 추출
        Like firstLike = likes.get(0);
        Like lastLike = likes.get(likes.size() - 1);

        // 3. 이미지 응답으로 변환
        List<ImageResponse> imageResponses = likes.stream()
                .map(like -> ImageResponse.of(like.getImage(), true))
                .toList();

        // 4. 이전/다음 페이지 커서 생성
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstLike.getImage().getId()),
                likeRepository.existsByImage_IdGreaterThanAndMemberId(
                        firstLike.getImage().getId(),
                        member.getId()
                )
        );

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastLike.getImage().getId()),
                likeRepository.existsByImage_IdLessThanAndMemberId(
                        lastLike.getImage().getId(),
                        member.getId()
                )
        );

        return new PageResponse<>(imageResponses, prevCursor, nextCursor);
    }


}
