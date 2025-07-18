package com.ll.demo03.videoTask.domain;

import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.domain.ImageTaskInitiate;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.VideoQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoTask {

    private final Long id;
    private final String prompt;
    private final String lora;
    private final String runpodId;
    private final String status;
    private final Long createdAt;
    private final Long modifiedAt;
    private final Member creator;

    @Builder
    public VideoTask(Long id, String prompt, String lora, String runpodId, String status,  Long createdAt, Long modifiedAt, Member creator) {
        this.id = id;
        this.prompt = prompt;
        this.lora = lora;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
    }

    public static VideoTask from(Member creator, VideoTaskInitiate videoTaskInitiate) {
        return VideoTask.builder()
                .prompt(videoTaskInitiate.getPrompt())
                .lora(videoTaskInitiate.getLora())
                .status(videoTaskInitiate.getStatus())
                .creator(creator)
                .build();
    }

    public static VideoTaskRequest updatePrompt(VideoTaskRequest request, Network network) {
        String newPrompt = network.modifyPrompt(request.getPrompt(), request.getLora());
        String finalPrompt = (newPrompt == null || newPrompt.isBlank()) ? request.getPrompt() : newPrompt;
        return new VideoTaskRequest(request.getLora(), finalPrompt);
    }

    public static VideoQueueRequest toQueueRequest(VideoTaskRequest request, Member creator) {
        return new VideoQueueRequest(request.getLora(), request.getPrompt(), creator.getId());
    }
}
