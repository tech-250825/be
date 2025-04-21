package com.ll.demo03.domain.mypage.service;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.folder.entity.Folder;
import com.ll.demo03.domain.mypage.folder.repository.FolderRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<ImageResponse> getMyImages(Member member, Pageable pageable) {
        Page<Image> images = imageRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);

        List<Long> imageIds = images.getContent().stream()
                .map(Image::getId)
                .collect(Collectors.toList());

        Set<Long> likedImageIds = imageIds.isEmpty() ?
                Collections.emptySet() :
                likeRepository.findImageIdsByImageIdInAndMemberId(imageIds, member.getId());

        return images.map(image ->
                new ImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getTask().getRawPrompt(),
                        image.getTask().getRatio(),
                        image.getLikeCount(),
                        likedImageIds.contains(image.getId()),
                        image.getCreatedAt()
                )
        );
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