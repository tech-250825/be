package com.ll.demo03.domain.member.controller;

import com.ll.demo03.domain.member.dto.UpdateNicknameRequest;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.service.MemberService;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.global.dto.GlobalResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = " 회원 API", description = "User")
public class MemberController {

    private MemberService memberService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<MemberDto> getUserProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        MemberDto userProfile = MemberDto.of(member);

        return GlobalResponse.success(userProfile);
    }

    @PutMapping("/nickname")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<MemberDto> updateNickname(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UpdateNicknameRequest request
    ) {
        Long memberId = principalDetails.user().getId();
        MemberDto updatedMember = memberService.updateNickname(memberId, request.getNickname());

        return GlobalResponse.success(updatedMember);
    }


}
