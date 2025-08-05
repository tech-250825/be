package com.ll.demo03.board.controller.port;

import com.ll.demo03.board.controller.request.BoardExportRequest;
import com.ll.demo03.board.controller.response.BoardExportResponse;
import com.ll.demo03.board.domain.BoardExport;
import com.ll.demo03.member.domain.Member;

import java.util.List;

public interface BoardExportService {
    BoardExportResponse exportBoardVideos(Long boardId, BoardExportRequest request, Member member);
    List<BoardExport> getExportHistory(Long boardId);
}