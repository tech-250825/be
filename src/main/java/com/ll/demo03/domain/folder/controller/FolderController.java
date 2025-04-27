package com.ll.demo03.domain.folder.controller;

import com.ll.demo03.domain.folder.dto.FolderImageResponse;
import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.folder.dto.FolderRequest;
import com.ll.demo03.domain.folder.dto.FolderResponse;
import com.ll.demo03.domain.folder.service.FolderService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folder")
@Slf4j
@Tag(name = "폴더 API", description = "폴더 관리 API")
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    @Operation(summary = "모든 폴더 조회", description = "내가 만든 모든 폴더를 조회합니다.")
    public ResponseEntity<Page<FolderResponse>> getFolders(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(folderService.getFolders(principalDetails.user(), pageable));
    }

    @PostMapping
    @Operation(summary="폴더 생성", description = "새로운 폴더를 생성합니다.")
    public ResponseEntity<?> createFolder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid FolderRequest requestDto
    ) {
        return ResponseEntity.ok(folderService.createFolder(principalDetails.user(), requestDto));
    }

    @DeleteMapping("/{folderId}")
    @Operation(summary = "폴더 삭제", description = "폴더를 삭제합니다.")
    public ResponseEntity<String> deleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.deleteFolder(principalDetails.user(), folderId);
        return ResponseEntity.ok("폴더가 성공적으로 삭제되었습니다.");
    }

    @PutMapping("/{folderId}")
    @Operation(summary = "폴더 이름 수정", description = "폴더의 이름을 수정합니다.")
    public ResponseEntity<?> modifyFolderName(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid FolderRequest requestDto
    ) {
        folderService.modifyFolderName(principalDetails.user(), folderId, requestDto);
        return ResponseEntity.ok(folderService.modifyFolderName(principalDetails.user(), folderId, requestDto));
    }

    @PostMapping("/{folderId}/images")
    @Operation(summary = "폴더에 이미지 추가", description = "폴더에 이미지를 추가합니다.")
    public ResponseEntity<String> addImagesToFolder(
            @PathVariable Long folderId,
            @RequestBody ImageIdsRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.addImagesToFolder(principalDetails.user(), folderId, request.getImageIds());
        return ResponseEntity.ok("폴더에 이미지가 추가되었습니다.");
    }

    @DeleteMapping("/{folderId}/images")
    @Operation(summary = "폴더에서 이미지 제거", description = "폴더에서 이미지를 제거합니다.")
    public ResponseEntity<String> removeImagesFromFolder(
            @PathVariable Long folderId,
            @RequestBody ImageIdsRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.removeImagesFromFolder(principalDetails.user(), folderId, request.getImageIds());
        return ResponseEntity.ok("폴더에서 이미지가 제거되었습니다.");
    }

    @GetMapping("/{folderId}/images")
    @Operation(summary = "폴더 내 이미지 조회", description = "폴더 내의 이미지를 조회합니다.")
    public ResponseEntity<FolderImageResponse> getImagesInFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable
    ) {
        FolderImageResponse folderImageResponse= folderService.getImagesInFolder(principalDetails.user(), folderId, cursorBasedPageable);
        return ResponseEntity.ok(folderImageResponse);
    }

}
