package com.ll.demo03.UGC.controller;

import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.UGC.service.UGCServiceImpl;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = " 회원 API", description = "User")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class UGCController {
    private final UGCServiceImpl UGCService;

    @GetMapping("/mypage")
    public GlobalResponse<PageResponse<List<UGCResponse>>> getMyImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) String type,
            CursorBasedPageable cursorBasedPageable
    ) {
        Member member = principalDetails.user();
        PageResponse<List<UGCResponse>> mypage= UGCService.getMyImages(member, type, cursorBasedPageable);
        return GlobalResponse.success( mypage);
    }
}