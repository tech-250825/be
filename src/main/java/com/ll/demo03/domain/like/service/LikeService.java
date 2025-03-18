package com.ll.demo03.domain.like.service;

import com.ll.demo03.domain.image.dto.ImageResponseDto;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.entity.Like;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
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

    @Transactional
    public void addLike(Long imageId, Member member) {

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + imageId));

        Like like = new Like();
        like.setMember(member);
        like.setImage(image);

        likeRepository.save(like);
    }

    @Transactional
    public void deleteLike(Long imageId, Member member) {

        Like like = likeRepository.findByMemberAndImageId(member, imageId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        likeRepository.delete(like);
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
