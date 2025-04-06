package com.ll.demo03.domain.like.controller;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.like.service.LikeService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{imageId}/like")
    public ResponseEntity<?>  addLike(
            @PathVariable(name="imageId") Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try {
            Member member = principalDetails.user();
            likeService.addLike(imageId, member);
            return ResponseEntity.ok().body(Map.of("message", "Like added successfully"));
        }catch (CustomException e){
            throw e;
        }
    }

    @DeleteMapping("/{imageId}/like")
    public ResponseEntity<?>  deleteLike(
            @PathVariable(name="imageId") Long imageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        try{
            Member member = principalDetails.user();
            likeService.deleteLike(imageId, member);
            return ResponseEntity.ok().body(Map.of("message", "Like deleted successfully"));
        }catch (CustomException e){
            throw e;
        }
    }

    @GetMapping("/mypage/like")
    public ResponseEntity<List<ImageResponse>> getMyLikes(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = principalDetails.user();
        List<ImageResponse> likes = likeService.getMyLikes(member);
        return ResponseEntity.ok(likes);
    }
}
