package com.ll.demo03.videoTask.controller.response;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.videoTask.domain.VideoTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VideoTaskResponse {

    private Long id;
    private String prompt;
    private String lora;
    private Status status;
    private String runpodId;
    private LocalDateTime createdAt;

    public static VideoTaskResponse from(VideoTask task) {
        return VideoTaskResponse.builder()
                .id(task.getId())
                .prompt(task.getPrompt())
                .lora(task.getLora())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
