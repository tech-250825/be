package com.ll.demo03.board.controller;

import com.ll.demo03.board.controller.port.BoardExportService;
import com.ll.demo03.board.controller.request.BoardExportRequest;
import com.ll.demo03.board.controller.response.BoardExportResponse;
import com.ll.demo03.board.domain.BoardExport;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardExportController {
    
    private final BoardExportService boardExportService;

    @PostMapping("/{boardId}/export")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<BoardExportResponse> exportBoardVideos(
            @PathVariable Long boardId,
            @RequestBody BoardExportRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        BoardExportResponse response = boardExportService.exportBoardVideos(boardId, request, member);
        return GlobalResponse.success(response);
    }

    @GetMapping("/{boardId}/exports")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse<List<BoardExport>> getExportHistory(
            @PathVariable Long boardId
    ) {
        List<BoardExport> exports = boardExportService.getExportHistory(boardId);
        return GlobalResponse.success(exports);
    }
}