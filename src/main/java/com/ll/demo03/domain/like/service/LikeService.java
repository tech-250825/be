package com.ll.demo03.domain.like.service;

import com.ll.demo03.domain.image.dto.ImageResponseDto;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
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
    private final SharedImageRepository sharedImageRepository;

    @Transactional
    public void addLike(Long imageId, Member member) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SharedImage sharedImage = sharedImageRepository.findByImageId(imageId);
        if (sharedImage == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        boolean alreadyLiked = likeRepository.existsByMemberIdAndImageId(member.getId(), imageId);
        if (alreadyLiked) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        Like like = new Like();
        like.setMember(member);
        like.setImage(image);
        likeRepository.save(like);

        sharedImage.setLikeCount(sharedImage.getLikeCount() + 1);
        sharedImageRepository.save(sharedImage);
    }

    @Transactional
    public void deleteLike(Long imageId, Member member) {

        Like like = likeRepository.findByMemberAndImageId(member, imageId)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likeRepository.delete(like);

        SharedImage sharedImage = sharedImageRepository.findByImageId(imageId);

        if (sharedImage == null) {
            throw new EntityNotFoundException("SharedImage not found for image id: " + imageId);
        }

        int newLikeCount = Math.max(0, sharedImage.getLikeCount() - 1);
        sharedImage.setLikeCount(newLikeCount);
        sharedImageRepository.save(sharedImage);
    }

    @Transactional
    public List<ImageResponseDto> getMyLikes(Member member) {
        List<Like> likes = likeRepository.findByMember(member);

        List<ImageResponseDto> responses = likes.stream()
                .map(like -> ImageResponseDto.of(like.getImage()))
                .collect(Collectors.toList());

        return responses;
    }
}
