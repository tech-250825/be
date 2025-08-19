package com.ll.demo03.imageTask.controller.response;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ImageTaskDto {

    private Long id;
    private String prompt;
    private String lora;
    private Status status;
    private String runpodId;
    private int width;
    private int height;
    private LocalDateTime createdAt;

    public static ImageTaskDto from(ImageTask task) {
        return ImageTaskDto.builder()
                .id(task.getId())
                .prompt(task.getOldPrompt())
                .lora(task.getCheckpoint().getName())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .width(task.getResolutionProfile().getWidth())
                .height(task.getResolutionProfile().getHeight())
                .createdAt(ImageTaskEntity.from(task).getCreatedAt())
                .build();
    }
}
