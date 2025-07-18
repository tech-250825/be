package com.ll.demo03.imageTask.controller.response;

import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.imageTask.domain.ImageTask;
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
    private UGCResponse image;

    public static TaskOrImageResponse fromTask(ImageTask task) {
        return TaskOrImageResponse.builder()
                .type("task")
                .task(ImageTaskDto.from(task))
                .build();
    }

    public static TaskOrImageResponse fromImage(ImageTask task, UGC UGC) {
        return TaskOrImageResponse.builder()
                .type("image")
                .task(ImageTaskDto.from(task))
                .image(UGCResponse.of(UGC))
                .build();
    }
}

