package com.ll.demo03.member.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.controller.port.MemberService;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.member.controller.response.MemberDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = " 회원 API", description = "User")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<MemberDto> getUserProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        MemberDto userProfile = MemberDto.of(member);

        return GlobalResponse.success(userProfile);
    }


    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자가 자신의 계정을 탈퇴합니다.")
    public GlobalResponse<Void> deleteMember(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long memberId = principalDetails.user().getId();
        memberService.delete(memberId);
        return GlobalResponse.success();
    }

    @PostMapping("/verify19")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<Void> verify19(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long memberId = principalDetails.user().getId();
        memberService.verify19(memberId);
        return GlobalResponse.success();
    }
}
