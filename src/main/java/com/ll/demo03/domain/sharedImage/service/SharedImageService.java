package com.ll.demo03.domain.sharedImage.service;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SharedImageService {

    private final SharedImageRepository sharedImageRepository;
    private final ImageRepository imageRepository;

    public Page<SharedImage> getAllSharedImages(Pageable pageable) {
        return sharedImageRepository.findAll(pageable);
    }

    public Page<SharedImage> getMySharedImages(Long memberId, Pageable pageable) {
        return sharedImageRepository.findAllByImage_Member_Id(memberId, pageable);
    }

    public Optional<SharedImage> getSharedImageById(Long id) {
        return sharedImageRepository.findById(id);
    }

    @Transactional
    public SharedImage createSharedImage(Long imageId) {

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SharedImage existingShare = sharedImageRepository.findByImageId(imageId);
        if (existingShare != null) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        SharedImage sharedImage = new SharedImage();
        sharedImage.setImage(image);
        sharedImage.setLikeCount(0);

        SharedImage saved = sharedImageRepository.save(sharedImage);

        return saved;
    }


    @Transactional
    public boolean deleteSharedImage(Long id, Long userId) {

        SharedImage sharedImage = sharedImageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!sharedImage.getImage().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        sharedImageRepository.delete(sharedImage);
        return true;
    }

}