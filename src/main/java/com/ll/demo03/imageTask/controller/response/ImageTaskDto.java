package com.ll.demo03.imageTask.controller.response;

import com.ll.demo03.imageTask.domain.ImageTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ImageTaskDto {

    private Long id;
    private String prompt;
    private String lora;
    private String status;
    private String runpodId;
    private Long createdAt;

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
