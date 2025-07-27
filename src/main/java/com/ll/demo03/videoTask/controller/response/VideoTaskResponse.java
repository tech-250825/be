package com.ll.demo03.videoTask.controller.response;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.videoTask.domain.VideoTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VideoTaskResponse {

    private Long id;
    private String prompt;
    private String lora;
    private String imageUrl;
    private int height;
    private int width;
    private int numFrames;
    private Status status;
    private String runpodId;
    private LocalDateTime createdAt;

    public static VideoTaskResponse from(VideoTask task) {

        String loraName = task.getLora() != null ? task.getLora().getName() : null;

        return VideoTaskResponse.builder()
                .id(task.getId())
                .prompt(task.getPrompt())
                .lora(loraName)
                .imageUrl(task.getImageUrl())
                .height(task.getHeight())
                .width(task.getWidth())
                .numFrames(task.getNumFrames())
                .status(task.getStatus())
                .runpodId(task.getRunpodId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
