package com.ll.demo03.board.controller.response;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private Long id;
    private String name;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private TaskOrVideoResponse latestVideoTask;

    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getMember().getId(),
                board.getCreatedAt(),
                board.getModifiedAt(),
                null
        );
    }

    public static BoardResponse from(Board board, TaskOrVideoResponse latestVideoTask) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getMember().getId(),
                board.getCreatedAt(),
                board.getModifiedAt(),
                latestVideoTask
        );
    }
}