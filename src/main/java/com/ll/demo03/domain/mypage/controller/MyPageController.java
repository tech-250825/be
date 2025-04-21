package com.ll.demo03.domain.mypage.controller;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.service.MyPageService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = " 회원 API", description = "User")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public Page<ImageResponse> getMyImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Member member = principalDetails.user();
        return myPageService.getMyImages(member, pageable);
    }

    @DeleteMapping("/mypage")
    public ResponseEntity<?> deleteMyImages(
            @RequestBody ImageIdsRequest imageIds,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        myPageService.deleteMyImages(imageIds, member);
        return ResponseEntity.ok().body(Map.of("message", "Images deleted successfully"));
    }
}