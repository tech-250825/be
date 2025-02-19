package com.ll.demo03.domain.image.controller;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.imageGenerate.repository.ImageGenerateRepository;
import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.service.MemberService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.oauth.token.TokenProvider;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ll.demo03.domain.image.dto.ImageResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
@Tag(name = " 회원 API", description = "User")
public class ImageController {

    private final ImageRepository imageRepository;


    @GetMapping("/my-image")
    public Page<ImageResponseDto> getMyImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Member member = principalDetails.user();
        Page<Image> images = imageRepository.findByImageGenerate_MemberIdOrderByCreatedAtDesc(member.getId(), pageable);
        return images.map(ImageResponseDto::new);
    }

    @DeleteMapping("/my-image/{imageId}")
    public ResponseEntity<GlobalResponse> deleteMyImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getImageGenerate().getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            imageRepository.delete(image);
            return ResponseEntity.ok(GlobalResponse.success());
        } catch (Exception e) {
            log.error("Error deleting image: ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}