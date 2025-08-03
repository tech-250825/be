package com.ll.demo03.board.controller;

import com.ll.demo03.board.controller.port.BoardService;
import com.ll.demo03.board.controller.request.BoardCreateRequest;
import com.ll.demo03.board.controller.response.BoardResponse;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<BoardResponse> create(
            @Valid @RequestBody BoardCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        BoardResponse response = boardService.create(request, member);
        return GlobalResponse.success(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<List<BoardResponse>> getMyBoards(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        List<BoardResponse> boards = boardService.getMyBoards(member);
        return GlobalResponse.success(boards);
    }
}
