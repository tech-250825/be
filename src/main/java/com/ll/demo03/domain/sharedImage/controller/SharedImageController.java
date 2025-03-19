package com.ll.demo03.domain.sharedImage.controller;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.dto.SharedImageResponseDto;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SharedImageController {

    private final SharedImageService sharedImageService;

    @GetMapping("/shared-images")
    public ResponseEntity<Page<SharedImageResponseDto>> getAllSharedImages(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<SharedImage> sharedImagesPage = sharedImageService.getAllSharedImages(pageableWithSort);
        Page<SharedImageResponseDto> dtoPage = sharedImagesPage.map(SharedImageResponseDto::of);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/mypage/shared-images")
    public ResponseEntity<Page<SharedImageResponseDto>> getMySharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Member member = principalDetails.user();
        Page<SharedImage> mySharedImagesPage = sharedImageService.getMySharedImages(member.getId(), pageable);
        Page<SharedImageResponseDto> dtoPage = mySharedImagesPage.map(SharedImageResponseDto::of);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/shared-images/{imageId}")
    public ResponseEntity<SharedImageResponseDto> getSharedImageById(@PathVariable Long imageId) {
        return sharedImageService.getSharedImageById(imageId)
                .map(sharedImage -> ResponseEntity.ok(SharedImageResponseDto.of(sharedImage)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/shared-images/{imageId}")
    public ResponseEntity<SharedImageResponseDto> createSharedImage(@PathVariable Long imageId) {
        try {
            SharedImage createdSharedImage = sharedImageService.createSharedImage(imageId);
            return ResponseEntity.ok(SharedImageResponseDto.of(createdSharedImage));
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