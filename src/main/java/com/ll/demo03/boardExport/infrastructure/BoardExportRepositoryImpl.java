package com.ll.demo03.boardExport.infrastructure;


import com.ll.demo03.boardExport.domain.BoardExport;
import com.ll.demo03.boardExport.service.port.BoardExportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BoardExportRepositoryImpl implements BoardExportRepository {
    
    private final BoardExportJpaRepository boardExportJpaRepository;

    @Override
    public BoardExport save(BoardExport boardExport) {
        BoardExportEntity entity = boardExport.toEntity();
        BoardExportEntity savedEntity = boardExportJpaRepository.save(entity);
        return BoardExport.from(savedEntity);
    }

    @Override
    public List<BoardExport> findByBoardIdOrderByCreatedAtDesc(Long boardId) {
        return boardExportJpaRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(BoardExport::from)
                .collect(Collectors.toList());
    }
}