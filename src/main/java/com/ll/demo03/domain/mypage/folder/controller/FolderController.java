package com.ll.demo03.domain.mypage.folder.controller;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.mypage.folder.dto.FolderRequest;
import com.ll.demo03.domain.mypage.folder.dto.FolderResponse;
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
    public ResponseEntity<Page<FolderResponse>> getFolders(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(folderService.getFolders(principalDetails.user(), pageable));
    }

    @PostMapping
    public ResponseEntity<?> createFolder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid FolderRequest requestDto
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
            @RequestBody @Valid FolderRequest requestDto
    ) {
        return ResponseEntity.ok(folderService.modifyFolderName(principalDetails.user(), folderId, requestDto));
    }

    @PostMapping("/{folderId}/images")
    public ResponseEntity<?> addImagesToFolder(
            @PathVariable Long folderId,
            @RequestBody ImageIdsRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.addImagesToFolder(principalDetails.user(), folderId, request.getImageIds());
        return ResponseEntity.ok().body(Map.of("message", "Images added to folder successfully"));
    }

    @DeleteMapping("/{folderId}/images")
    public ResponseEntity<?> removeImagesFromFolder(
            @PathVariable Long folderId,
            @RequestBody ImageIdsRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        folderService.removeImagesFromFolder(principalDetails.user(), folderId, request.getImageIds());
        return ResponseEntity.ok().body(Map.of("message", "Images removed from folder successfully"));
    }

    @GetMapping("/{folderId}/images")
    public ResponseEntity<?> getImagesInFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(folderService.getImagesInFolder(principalDetails.user(), folderId, pageable));
    }

}
