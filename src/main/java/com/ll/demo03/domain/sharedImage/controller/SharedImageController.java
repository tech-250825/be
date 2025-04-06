package com.ll.demo03.domain.sharedImage.controller;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponse;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.service.SharedImageService;
import com.ll.demo03.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SharedImageController {

    private final SharedImageService sharedImageService;

    @GetMapping("/shared-images")
    public ResponseEntity<Page<SharedImageResponse>> getAllSharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Long currentMemberId = principalDetails != null ? principalDetails.user().getId() : null;

        Page<SharedImageResponse> dtoPage = sharedImageService.getAllSharedImageResponses(currentMemberId, pageableWithSort);
        return ResponseEntity.ok(dtoPage);
    }


    @GetMapping("/mypage/shared-images")
    public ResponseEntity<Page<SharedImageResponse>> getMySharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Member member = principalDetails.user();
        Page<SharedImageResponse> dtoPage = sharedImageService.getMySharedImages(member.getId(), pageable);

        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/shared-images/{imageId}")
    public ResponseEntity<String> createSharedImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try {
            sharedImageService.createSharedImage(imageId);

            return ResponseEntity.ok("이미지가 성공적으로 공유되었습니다.");
        } catch (CustomException e) {
            throw e;
        }
    }

    @DeleteMapping("/shared-images/{imageId}")
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