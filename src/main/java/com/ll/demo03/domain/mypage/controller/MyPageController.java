package com.ll.demo03.domain.mypage.controller;


import com.ll.demo03.domain.image.dto.ImageResponseDto;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Tag(name = " 회원 API", description = "User")
public class MyPageController {

    private final ImageRepository imageRepository;


    @GetMapping("/mypage")
    public Page<ImageResponseDto> getMyImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Member member = principalDetails.user();
        Page<Image> images = imageRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);
        return images.map(ImageResponseDto::of);
    }

    @DeleteMapping("/mypage/{imageId}")
    public ResponseEntity<GlobalResponse> deleteMyImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            imageRepository.delete(image);
            return ResponseEntity.ok(GlobalResponse.success());
        } catch (Exception e) {
            log.error("Error deleting mypage: ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/mypage/{imageId}/bookmark")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!image.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to share this mypage");
        }

        image.toggleBookmark();
        imageRepository.save(image);

        return ResponseEntity.ok(Map.of(
                "imageId", image.getId(),
                "shared", image.getIsBookmarked()
        ));
    }

    @GetMapping("/bookmark")
    public Page<ImageResponseDto> getBookmarkedImages(
            @PageableDefault(page = 0, size = 12) Pageable pageable
    ) {
        Page<Image> sharedImages = imageRepository.findByBookmarkedOrderByCreatedAtDesc(true, pageable);
        return sharedImages.map(ImageResponseDto::of);
    }
}