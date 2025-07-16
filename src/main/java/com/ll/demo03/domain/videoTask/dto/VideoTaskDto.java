package com.ll.demo03.domain.videoTask.dto;

import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VideoTaskDto {

    private Long id;
    private String prompt;
    private String lora;
    private String status;
    private String runpodId;
    private LocalDateTime createdAt;

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
