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

        if (!pageable.hasCursors()) {
            likeSlice = likeRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("member"), member),
                    ofSize(pageable.getSize())
            );
        } else if (pageable.hasPrevPageCursor()) {
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));

            Specification<Like> cursorSpec = (root, query, cb) ->
                    cb.and(
                            cb.equal(root.get("member"), member),
                            cb.greaterThan(root.get("image").get("id"), cursorId)
                    );

            Sort ascSort = Sort.by(Sort.Direction.ASC, "image.id");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            likeSlice = likeRepository.findAll(cursorSpec, pageRequest);
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

        if (!likeSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Like> likes = likeSlice.getContent();

        Like firstLike = likes.get(0);
        Like lastLike = likes.get(likes.size() - 1);

        boolean isFirstPage = !pageable.hasCursors();

        List<ImageResponse> imageResponses = likes.stream()
                .map(like -> ImageResponse.of(like.getImage(), true))
                .toList();

        String prevCursor = isFirstPage ? null : pageable.getEncodedCursor(
                String.valueOf(firstLike.getImage().getId()),
                true
        );

        String nextCursor = likeSlice.hasNext() ? pageable.getEncodedCursor(
                String.valueOf(lastLike.getImage().getId()),
                true
        ) : null;

        return new PageResponse<>(imageResponses, prevCursor, nextCursor);
    }


}
