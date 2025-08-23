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
public class I2ITaskDto {
    private Long id;
    private String prompt;
    private String imageUrl;
    private Status status;
    private String runpodId;
    private LocalDateTime createdAt;

    public static I2ITaskDto from(ImageTask task) {
        return I2ITaskDto.builder()
                .id(task.getId())
                .prompt(task.getOldPrompt() != null ? task.getOldPrompt() : task.getPrompt())
                .imageUrl(task.getImageUrl())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .createdAt(ImageTaskEntity.from(task).getCreatedAt())
                .build();
    }
}
