package com.ll.demo03.domain.folder.service;

import com.ll.demo03.domain.folder.dto.FolderImageResponse;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.dto.PublicMemberDto;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.folder.dto.FolderRequest;
import com.ll.demo03.domain.folder.dto.FolderResponse;
import com.ll.demo03.domain.folder.entity.Folder;
import com.ll.demo03.domain.folder.repository.FolderRepository;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.CursorPagingUtils;
import com.ll.demo03.global.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.domain.PageRequest.ofSize;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    public PageResponse<List<FolderResponse>> getFolders(Member member, CursorBasedPageable pageable) {
        Slice<Folder> foldersPage;

        // 1. 커서 방향에 따른 처리
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 - createdAt 기준 내림차순
            Sort descSort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), descSort);
            foldersPage = folderRepository.findByMember(member, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            // 역방향 스펙 생성 (createdAt이 더 큰 항목)
            Specification<Folder> cursorSpec = (root, query, cb) ->
                    cb.and(cb.equal(root.get("member"), member),
                            cb.greaterThan(root.get("createdAt"), cursorCreatedAt));

            // createdAt 오름차순으로 조회
            Sort ascSort = Sort.by(Sort.Direction.ASC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), ascSort);

            foldersPage = folderRepository.findAll(cursorSpec, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 정렬 유지 (최신순)
            List<Folder> content = new ArrayList<>(foldersPage.getContent());
            Collections.reverse(content);
            foldersPage = new SliceImpl<>(content, pageRequest, foldersPage.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            // 다음 페이지 스펙 (createdAt이 더 작은 항목)
            Specification<Folder> cursorSpec = (root, query, cb) ->
                    cb.and(cb.equal(root.get("member"), member),
                            cb.lessThan(root.get("createdAt"), cursorCreatedAt));

            // createdAt 내림차순으로 조회
            Sort descSort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), descSort);
            foldersPage = folderRepository.findAll(cursorSpec, pageRequest);
        }

        // 2. 결과가 없을 경우 빈 응답 반환
        if (!foldersPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<Folder> folders = foldersPage.getContent();
        Folder firstFolder = folders.get(0);
        Folder lastFolder = folders.get(folders.size() - 1);

        // 3. 응답 데이터 준비
        List<FolderResponse> responseList = folders.stream()
                .map(FolderResponse::of)
                .collect(Collectors.toList());

        // 4. 이전/다음 페이지 커서 생성 (createdAt 기준)
        String prevCursor = pageable.getEncodedCursor(
                firstFolder.getCreatedAt().toString(),
                folderRepository.existsByMemberAndCreatedAtGreaterThan(member, firstFolder.getCreatedAt())
        );

        String nextCursor = pageable.getEncodedCursor(
                lastFolder.getCreatedAt().toString(),
                folderRepository.existsByMemberAndCreatedAtLessThan(member, lastFolder.getCreatedAt())
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
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
    public FolderImageResponse getImagesInFolder(Member member, Long folderId, CursorBasedPageable pageable) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 커서 방향에 따른 이미지 조회
        Slice<Image> imageSlice;

        if (!pageable.hasCursors()) {
            // 첫 페이지 요청
            imageSlice = imageRepository.findByFolderIdOrderByIdDesc(folderId, ofSize(pageable.getSize()));
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지로 이동 (위로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));

            // ID 오름차순으로 조회 (더 큰 ID)
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), Sort.by(Sort.Direction.ASC, "id"));
            imageSlice = imageRepository.findByFolderIdAndIdGreaterThanOrderByIdAsc(
                    folderId, cursorId, pageRequest);

            // 결과를 역순으로 바꿔서 일관된 내림차순 표시 유지
            List<Image> content = new ArrayList<>(imageSlice.getContent());
            Collections.reverse(content);
            imageSlice = new SliceImpl<>(content, pageRequest, imageSlice.hasNext());
        } else {
            // 다음 페이지로 이동 (아래로 스크롤)
            Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
            imageSlice = imageRepository.findByFolderIdAndIdLessThanOrderByIdDesc(
                    folderId, cursorId, ofSize(pageable.getSize()));
        }

        // 3. 결과가 없는 경우 빈 응답 반환
        if (!imageSlice.hasContent()) {
            return FolderImageResponse.builder()
                    .id(folder.getId())
                    .name(folder.getName())
                    .images(new PageResponse<>(Collections.emptyList(), null, null))
                    .build();
        }

        // 4. 이미지 정보 처리
        List<Image> images = imageSlice.getContent();
        Image firstImage = images.get(0);
        Image lastImage = images.get(images.size() - 1);

        // 5. 좋아요 정보 조회
        List<Long> imageIds = images.stream()
                .map(Image::getId)
                .toList();

        Set<Long> likedImageIds = imageIds.isEmpty() ?
                Collections.emptySet() :
                likeRepository.findImageIdsByImageIdInAndMemberId(imageIds, member.getId());

        // 6. 이미지 응답 생성
        List<ImageResponse> imageResponses = images.stream()
                .map(image -> new ImageResponse(
                        PublicMemberDto.of(image.getMember()),
                        image.getId(),
                        image.getUrl(),
                        image.getTask().getRawPrompt(),
                        image.getTask().getRatio(),
                        image.getLikeCount(),
                        likedImageIds.contains(image.getId()),
                        image.getIsShared(),
                        image.getUpscaleTask() != null,
                        image.getTask().getTaskId(),
                        image.getImgIndex(),
                        image.getCreatedAt()
                ))
                .toList();

        // 7. 이전/다음 페이지 커서 생성
        String prevCursor = pageable.getEncodedCursor(
                String.valueOf(firstImage.getId()),
                imageRepository.existsByFolderIdAndIdGreaterThan(folderId, firstImage.getId())
        );

        String nextCursor = pageable.getEncodedCursor(
                String.valueOf(lastImage.getId()),
                imageRepository.existsByFolderIdAndIdLessThan(folderId, lastImage.getId())
        );

        // 8. 페이지 응답 구성
        PageResponse<List<ImageResponse>> pageResponse = new PageResponse<>(
                imageResponses, prevCursor, nextCursor
        );

        // 9. 최종 응답 구성
        return FolderImageResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .images(pageResponse)
                .build();
    }
}
