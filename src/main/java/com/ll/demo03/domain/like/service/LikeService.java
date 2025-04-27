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
import java.util.Collections;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        Slice<Like> likeSlice = likeRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("member"), member),
                ofSize(pageable.getSize())
        );

        if (!likeSlice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Like> likes = likeSlice.getContent();
        List<ImageResponse> imageResponses = likes.stream()
                .map(like -> ImageResponse.of(like.getImage(), true))
                .collect(Collectors.toList());

        return new PageResponse<>(
                imageResponses,
                pageable.getEncodedCursor(
                        String.valueOf(likes.get(0).getImage().getId()),
                        likeRepository.existsByImageIdLessThanAndMemberId(
                                likes.get(0).getImage().getId(),
                                member.getId()
                        )
                ),
                pageable.getEncodedCursor(
                        String.valueOf(likes.get(likes.size() - 1).getImage().getId()),
                        likeSlice.hasNext()
                )
        );
    }


}
