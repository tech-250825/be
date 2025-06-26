package com.ll.demo03.domain.member.controller;

import com.ll.demo03.domain.member.dto.UpdateNicknameRequest;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.service.MemberService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.member.dto.MemberDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = " 회원 API", description = "User")
public class MemberController {

    private final MemberService memberService;
    private final SharedImageRepository sharedImageRepository;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberDto> getUserProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        long sharedImageCount = sharedImageRepository.countByMemberId(member.getId());
        MemberDto userProfile = MemberDto.of(member, sharedImageCount);

        return ResponseEntity.ok(userProfile);
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유저 이름과 프로필 이미지 변경", description = "유저 이름과 프로필 사진을 변경합니다")
    public ResponseEntity<MemberDto> updateProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("nickname") @NotBlank @Size(min = 2, max = 20) String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Long memberId = principalDetails.user().getId();
        MemberDto updatedMember = memberService.updateProfile(
                memberId,
                nickname,
                profileImage
        );

        return ResponseEntity.ok(updatedMember);
    }

    @PutMapping("/nickname")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberDto> updateNickname(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateNicknameRequest request
    ) {
        Long memberId = principalDetails.user().getId();
        MemberDto updatedMember = memberService.updateNickname(memberId, request.getNickname());

        return ResponseEntity.ok(updatedMember);
    }


}
