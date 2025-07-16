package com.ll.demo03.domain.videoTask.dto;

import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.imageTask.dto.ImageTaskDto;
import com.ll.demo03.domain.imageTask.dto.TaskOrImageResponse;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.rekognition.model.Video;


@Getter
@AllArgsConstructor
@Builder
public class TaskOrVideoResponse {
    private String type; // "task" or "image"

    @Nullable
    private VideoTaskDto task;

    @Nullable
    private ImageResponse image;

    public static TaskOrVideoResponse fromTask(VideoTask task) {
        return TaskOrVideoResponse.builder()
                .type("task")
                .task(VideoTaskDto.from(task))
                .build();
    }

    public static TaskOrVideoResponse fromImage(VideoTask task, Image image) {
        return TaskOrVideoResponse.builder()
                .type("video")
                .task(VideoTaskDto.from(task))
                .image(ImageResponse.of(image))
                .build();
    }
}
