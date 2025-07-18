package com.ll.demo03.videoTask.controller.response;

import com.ll.demo03.videoTask.domain.VideoTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VideoTaskDto {

    private Long id;
    private String prompt;
    private String lora;
    private String status;
    private String runpodId;
    private Long createdAt;

    public static VideoTaskDto from(VideoTask task) {
        return VideoTaskDto.builder()
                .id(task.getId())
                .prompt(task.getPrompt())
                .lora(task.getLora())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
