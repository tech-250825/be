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
public class TaskOrI2IResponse {
    private String type; // "task" or "image"

    @Nullable
    private I2ITaskDto task;

    @Nullable
    private UGCResponse image;

    public static TaskOrI2IResponse fromTask(ImageTask task) {
        return TaskOrI2IResponse.builder()
                .type("task")
                .task(I2ITaskDto.from(task))
                .build();
    }

    public static TaskOrI2IResponse fromImage(ImageTask task, UGC UGC) {
        return TaskOrI2IResponse.builder()
                .type("image")
                .task(I2ITaskDto.from(task))
                .image(UGCResponse.of(UGC))
                .build();
    }
}
