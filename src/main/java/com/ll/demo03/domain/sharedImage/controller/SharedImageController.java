package com.ll.demo03.domain.sharedImage.controller;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponse;
import com.ll.demo03.domain.sharedImage.dto.SharedImagesResponse;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.service.SharedImageService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.global.util.PageSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "공유된 이미지 API", description = "공유된 이미지 관리 API")
public class SharedImageController {

    private final SharedImageService sharedImageService;

    @GetMapping("/shared-images")
    @Operation(summary = "모든 공유된 이미지 조회", description= "모든 공유된 이미지 조회")
    public ResponseEntity<PageResponse<List<SharedImagesResponse>>> getAllSharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) String type,
            CursorBasedPageable cursorBasedPageable
    ) {
        Long currentMemberId = principalDetails != null ? principalDetails.user().getId() : null;

        Specification<SharedImage> typeSpec = createTypeSpecification(type);

        PageResponse<List<SharedImagesResponse>> dtoPage = sharedImageService.getAllSharedImageResponses(
                currentMemberId,
                typeSpec,
                cursorBasedPageable
        );
        return ResponseEntity.ok(dtoPage);
    }

    private Specification<SharedImage> createTypeSpecification(String type) {
        if ("video".equalsIgnoreCase(type)) {
            return (root, query, cb) -> cb.isNotNull(root.get("image").get("videoTask"));
        } else if ("image".equalsIgnoreCase(type)) {
            return (root, query, cb) -> cb.isNull(root.get("image").get("videoTask"));
        } else {
            return (root, query, cb) -> cb.conjunction();
        }
    }



    @GetMapping("/mypage/shared-images")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내가 공유한 이미지 조회", description= "내가 공유한 이미지 조회")
    public ResponseEntity<PageResponse<List<SharedImagesResponse>>> getMySharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable
    ) {
        Member member = principalDetails.user();

        PageResponse<List<SharedImagesResponse>> dtoPage = sharedImageService.getMySharedImages(member.getId(), cursorBasedPageable);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/shared-images/{imageId}")
    @Operation(summary = "특정 공유된 이미지 조회", description= "특정 공유된 이미지 조회")
    public ResponseEntity<SharedImageResponse> getSharedImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable
    ) {
        try {
            Long currentMemberId = principalDetails != null ? principalDetails.user().getId() : null;
            PageSpecification specification = new PageSpecification<>("id", cursorBasedPageable);
            SharedImageResponse image = sharedImageService.getSharedImage(currentMemberId, specification, cursorBasedPageable, imageId);

            return ResponseEntity.ok(image);
        } catch (CustomException e) {
            throw e;
        }
    }


    @PostMapping("/shared-images/{imageId}")
    @Operation(summary = "이미지 공유", description= "내 이미지 공유")
    public GlobalResponse<String> createSharedImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try {
            sharedImageService.createSharedImage(imageId);

            return GlobalResponse.success("이미지가 성공적으로 공유되었습니다.");
        } catch (CustomException e) {
            throw e;
        }
    }

    @DeleteMapping("/shared-images/{imageId}")
    @Operation(summary = "이미지 공유 취소", description= "내 이미지 공유 취소")
    public ResponseEntity<Void> deleteSharedImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        try {
            boolean deleted = sharedImageService.deleteSharedImage(imageId, member.getId());
            return deleted ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (CustomException e) {
            throw e;
        }
    }
}