package com.ll.demo03.domain.mypage.controller;

import com.ll.demo03.domain.image.dto.ImageIdsRequest;
import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.mypage.service.MyPageService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.global.util.PageSpecification;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = " 회원 API", description = "User")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/mypage")
    public ResponseEntity<PageResponse<List<ImageResponse>>> getMyImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            CursorBasedPageable cursorBasedPageable,
            @RequestParam(required = false) String type
    ) {
        Member member = principalDetails.user();

        PageResponse<List<ImageResponse>> mypage=myPageService.getMyImages(member, cursorBasedPageable, type);
        return ResponseEntity.ok( mypage);
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