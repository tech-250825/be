package com.ll.demo03.domain.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.domain.member.service.MemberService;
import com.ll.demo03.domain.oauth.token.TokenProvider;
import com.ll.demo03.global.dto.GlobalResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = " 회원 API", description = "User")
public class MemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<MemberDto> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        System.out.println(authentication);
        MemberDto userProfile = memberService.findMemberByEmail(email);

        return GlobalResponse.success(userProfile);
    }
}
