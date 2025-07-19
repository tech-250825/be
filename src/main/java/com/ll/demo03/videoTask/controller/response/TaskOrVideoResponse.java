package com.ll.demo03.videoTask.controller.response;

import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.videoTask.domain.VideoTask;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
public class TaskOrVideoResponse {
    private String type; // "task" or "video"

    @Nullable
    private VideoTaskResponse task;

    @Nullable
    private UGCResponse image;

    public static TaskOrVideoResponse fromTask(VideoTask task) {
        return TaskOrVideoResponse.builder()
                .type("task")
                .task(VideoTaskResponse.from(task))
                .build();
    }

    public static TaskOrVideoResponse fromVideo(VideoTask task, UGC UGC) {
        return TaskOrVideoResponse.builder()
                .type("video")
                .task(VideoTaskResponse.from(task))
                .image(UGCResponse.of(UGC))
                .build();
    }
}
