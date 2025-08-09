package com.ll.demo03.boardExport.controller.port;


import com.ll.demo03.boardExport.controller.request.BoardExportRequest;
import com.ll.demo03.boardExport.controller.response.BoardExportResponse;
import com.ll.demo03.boardExport.domain.BoardExport;
import com.ll.demo03.member.domain.Member;

import java.util.List;

public interface BoardExportService {
    BoardExportResponse exportBoardVideos(Long boardId, BoardExportRequest request, Member member);
    List<BoardExport> getExportHistory(Long boardId);
}