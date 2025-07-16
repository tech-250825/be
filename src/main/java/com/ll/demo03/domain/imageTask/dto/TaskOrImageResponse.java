package com.ll.demo03.domain.imageTask.dto;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TaskOrImageResponse {
    private String type; // "task" or "image"

    @Nullable
    private ImageTaskDto task;

    @Nullable
    private ImageResponse image;

    public static TaskOrImageResponse fromTask(ImageTask task) {
        return TaskOrImageResponse.builder()
                .type("task")
                .task(ImageTaskDto.from(task))
                .build();
    }

    public static TaskOrImageResponse fromImage(ImageTask task, Image image) {
        return TaskOrImageResponse.builder()
                .type("image")
                .task(ImageTaskDto.from(task))
                .image(ImageResponse.of(image))
                .build();
    }
}

