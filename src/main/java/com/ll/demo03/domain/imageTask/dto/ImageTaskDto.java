package com.ll.demo03.domain.imageTask.dto;

import com.ll.demo03.domain.imageTask.entity.ImageTask;
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
    private String status;
    private String runpodId;
    private LocalDateTime createdAt;

    public static ImageTaskDto from(ImageTask task) {
        return ImageTaskDto.builder()
                .id(task.getId())
                .prompt(task.getPrompt())
                .lora(task.getLora())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
