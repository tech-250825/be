package com.ll.demo03.domain.mypage.folder.controller;

import com.ll.demo03.domain.mypage.folder.dto.FolderRequestDto;
import com.ll.demo03.domain.mypage.folder.dto.FolderResponseDto;
import com.ll.demo03.domain.mypage.folder.service.FolderService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
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
@Tag(name = "폴더 API", description = "Folder Management")
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public ResponseEntity<Page<FolderResponseDto>> getFolders(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(folderService.getFolders(principalDetails.user(), pageable));
    }

    @PostMapping
    public ResponseEntity<?> createFolder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid FolderRequestDto requestDto
    ) {
        return ResponseEntity.ok(folderService.createFolder(principalDetails.user(), requestDto));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.deleteFolder(principalDetails.user(), folderId);
        return ResponseEntity.ok().body(Map.of("message", "Folder deleted successfully"));
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<?> modifyFolderName(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid FolderRequestDto requestDto
    ) {
        return ResponseEntity.ok(folderService.modifyFolderName(principalDetails.user(), folderId, requestDto));
    }

    @PostMapping("/{folderId}/images/{imageId}")
    public ResponseEntity<?> addImageToFolder(
            @PathVariable Long folderId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.addImageToFolder(principalDetails.user(), folderId, imageId);
        return ResponseEntity.ok().body(Map.of("message", "Image added to folder successfully"));
    }

    @DeleteMapping("/{folderId}/images/{imageId}")
    public ResponseEntity<?> removeImageFromFolder(
            @PathVariable Long folderId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.removeImageFromFolder(principalDetails.user(), folderId, imageId);
        return ResponseEntity.ok().body(Map.of("message", "Image removed from folder successfully"));
    }

}
