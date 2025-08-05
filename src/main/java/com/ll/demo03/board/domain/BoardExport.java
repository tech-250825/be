package com.ll.demo03.board.domain;

import com.ll.demo03.board.infrastructure.BoardExportEntity;
import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardExport {
    private final Long id;
    private final Long boardId;
    private final String videoUrl;
    private final String status;
    private final Double duration;
    private final Long fileSize;
    private final LocalDateTime createdAt;
    private final Member member;

    public static BoardExport from(BoardExportEntity entity) {
        return BoardExport.builder()
                .id(entity.getId())
                .boardId(entity.getBoardId())
                .videoUrl(entity.getVideoUrl())
                .status(entity.getStatus())
                .duration(entity.getDuration())
                .fileSize(entity.getFileSize())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static BoardExport create(Long boardId, String videoUrl, Double duration, Long fileSize, Member member) {
        return BoardExport.builder()
                .boardId(boardId)
                .videoUrl(videoUrl)
                .status("COMPLETED")
                .duration(duration)
                .fileSize(fileSize)
                .createdAt(LocalDateTime.now())
                .member(member)
                .build();
    }

    public BoardExportEntity toEntity() {
        return BoardExportEntity.builder()
                .id(this.id)
                .boardId(this.boardId)
                .videoUrl(this.videoUrl)
                .status(this.status)
                .duration(this.duration)
                .fileSize(this.fileSize)
                .createdAt(this.createdAt)
                .build();
    }
}