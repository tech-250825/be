package com.ll.demo03.domain.like.controller;

import com.ll.demo03.domain.image.dto.ImageResponseDto;
import com.ll.demo03.domain.like.service.LikeService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.global.dto.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{imageId}/like")
    public GlobalResponse addBookmark(
            @PathVariable(name="imageId") Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        likeService.addLike(imageId, member);
        return GlobalResponse.success("좋아요 등록에 성공했습니다.");
    }

    @DeleteMapping("/{imageId}/like")
    public GlobalResponse deleteBookmark(
            @PathVariable(name="imageId") Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        likeService.deleteLike(imageId, member);
        return GlobalResponse.success("좋아요 삭제에 성공했습니다.");
    }

    @GetMapping("/like/my")
    public GlobalResponse<?> getMyBookmarks(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = principalDetails.user();
        List<ImageResponseDto> bookmarks = likeService.getMyLikes(member);
        return GlobalResponse.success(bookmarks);
    }
}
