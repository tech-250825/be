package com.ll.demo03.board.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardExportResponse {
    private boolean success;
    private String taskId;
    private String message;
    private String estimatedTime;
    private String downloadUrl; // When export is complete
    
    public static BoardExportResponse success(String taskId) {
        return BoardExportResponse.builder()
                .success(true)
                .taskId(taskId)
                .message("Video export started")
                .estimatedTime("2-5 minutes")
                .build();
    }
    
    public static BoardExportResponse completed(String downloadUrl) {
        return BoardExportResponse.builder()
                .success(true)
                .message("Video export completed")
                .downloadUrl(downloadUrl)
                .build();
    }
}