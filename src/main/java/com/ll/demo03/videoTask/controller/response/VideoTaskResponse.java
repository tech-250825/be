package com.ll.demo03.videoTask.controller.response;

import com.ll.demo03.videoTask.domain.VideoTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VideoTaskResponse {

    private Long id;
    private String prompt;
    private String lora;
    private String status;
    private String runpodId;
    private Long createdAt;

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
