package com.ll.demo03.boardExport.service.port;



import com.ll.demo03.boardExport.domain.BoardExport;

import java.util.List;

public interface BoardExportRepository {
    BoardExport save(BoardExport boardExport);
    List<BoardExport> findByBoardIdOrderByCreatedAtDesc(Long boardId);
}