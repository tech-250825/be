package com.ll.demo03.domain.mypage.folder.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.folder.dto.FolderResponseDto;
import com.ll.demo03.domain.mypage.folder.entity.Folder;
import com.ll.demo03.domain.mypage.folder.repository.FolderRepository;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;

    public Page<FolderResponseDto> getFolders(Member member, Pageable pageable) {
        return folderRepository.findByMemberOrderByCreatedAtDesc(member, pageable)
                .map(FolderResponseDto::of);
    }

    @Transactional
    public Map<String, Object> createFolder(Member member, FolderResponseDto requestDto) {
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
    public Map<String, Object> modifyFolderName(Member member, Long folderId, FolderResponseDto requestDto) {
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
    public void addImageToFolder(Member member, Long folderId, Long imageId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (folder.getImages().contains(image)) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        folder.getImages().add(image);
        folderRepository.save(folder);
    }

    @Transactional
    public void removeImageFromFolder(Member member, Long folderId, Long imageId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getImages().contains(image)) {
            throw new CustomException(ErrorCode.DUPLICATED_METHOD);
        }

        folder.getImages().remove(image);

        folderRepository.save(folder);
    }
}
