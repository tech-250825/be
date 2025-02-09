package com.ll.demo03.domain.prompt.controller;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.prompt.service.PromptService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class PromptController {
    private final MemberRepository memberRepository;
    private final PromptService promptService;

    @PostMapping(value = "/prompt/create")
    @PreAuthorize("isAuthenticated()")
    public String createImage(@RequestBody Map<String, String> data, Authentication authentication) {

        Optional<Member> optionalMember = memberRepository.findByEmail(authentication.getName());
        Member member = optionalMember.orElseThrow(() -> new RuntimeException("Member not found"));

        String gptResponse = promptService.sendToGpt(data, member);

        return gptResponse;
    }
}
