package com.ll.demo03.videoTask.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VideoTaskCreationResponse {
    private String timestamp;
    private int statusCode;
    private String message;
    private TaskData data;
    
    @Getter
    @Builder
    public static class TaskData {
        private Long taskId;
        private String status;
    }
    
    public static VideoTaskCreationResponse success(Long taskId) {
        return VideoTaskCreationResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .statusCode(200)
                .message("Video generation task created successfully")
                .data(TaskData.builder()
                        .taskId(taskId)
                        .status("IN_PROGRESS")
                        .build())
                .build();
    }
}