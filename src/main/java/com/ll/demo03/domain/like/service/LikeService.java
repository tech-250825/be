package com.ll.demo03.domain.like.service;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public List<ImageResponse> getMyLikes(Member member) {
        List<Like> likes = likeRepository.findByMember(member);

        List<ImageResponse> responses = likes.stream()
                .map(like -> ImageResponse.of(like.getImage(), true))
                .collect(Collectors.toList());

        return responses;
    }
}
