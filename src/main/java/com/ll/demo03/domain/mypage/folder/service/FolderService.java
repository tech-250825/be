package com.ll.demo03.domain.mypage.folder.service;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.folder.dto.FolderRequest;
import com.ll.demo03.domain.mypage.folder.dto.FolderResponse;
import com.ll.demo03.domain.mypage.folder.entity.Folder;
import com.ll.demo03.domain.mypage.folder.repository.FolderRepository;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    public Page<FolderResponse> getFolders(Member member, Pageable pageable) {
        return folderRepository.findByMemberOrderByCreatedAtDesc(member, pageable)
                .map(FolderResponse::of);
    }

    @Transactional
    public Map<String, Object> createFolder(Member member, FolderRequest requestDto) {
        Folder folder = Folder.builder()
                .name(requestDto.getName())
                .member(member)
                .build();
        folderRepository.save(folder);

        return Map.of(
                "folderId", folder.getId(),
                "message", "Folder created successfully"
        );
    }

    @Transactional
    public void deleteFolder(Member member, Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        validateFolderOwnership(folder, member);
        folderRepository.delete(folder);
    }

    @Transactional
    public Map<String, Object> modifyFolderName(Member member, Long folderId, FolderRequest requestDto) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        validateFolderOwnership(folder, member);
        folder.updateName(requestDto.getName());
        folderRepository.save(folder);

        return Map.of(
                "folderId", folder.getId(),
                "newName", folder.getName()
        );
    }

    private void validateFolderOwnership(Folder folder, Member member) {
        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Transactional
    public void addImagesToFolder(Member member, Long folderId, List<Long> imageIds) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Image> images = imageRepository.findAllById(imageIds);

        if (images.size() != imageIds.size()) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        List<Image> duplicateImages = images.stream()
                .filter(image -> image.getFolder() != null && image.getFolder().getId().equals(folderId))
                .collect(Collectors.toList());

        if (!duplicateImages.isEmpty()) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        images.forEach(image -> image.setFolder(folder));
        imageRepository.saveAll(images);
    }

    @Transactional
    public void removeImagesFromFolder(Member member, Long folderId, List<Long> imageIds) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Image> images = imageRepository.findAllById(imageIds);

        if (images.size() != imageIds.size()) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        List<Image> notInFolderImages = images.stream()
                .filter(image -> image.getFolder() == null || !image.getFolder().getId().equals(folderId))
                .collect(Collectors.toList());

        if (!notInFolderImages.isEmpty()) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        images.forEach(image -> image.setFolder(null));
        imageRepository.saveAll(images);
    }

    @Transactional(readOnly = true)
    public Page<ImageResponse> getImagesInFolder(Member member, Long folderId, Pageable pageable) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Page<Image> images = imageRepository.findByFolderId(folderId, pageable);

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
}
