package com.ll.demo03.imageTask.domain;

import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageTask {

    private final Long id;
    private final String prompt;
    private final String lora;
    private final String runpodId;
    private final String status;
    private final Long createdAt;
    private final Long modifiedAt;
    private final Member creator;

    @Builder
    public ImageTask(Long id, String prompt, String lora, String runpodId, String status, Long createdAt, Long modifiedAt, Member creator) {
        this.id = id;
        this.prompt = prompt;
        this.lora = lora;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
    }

    public static ImageTask from(Member creator, ImageTaskInitiate imageTaskInitiate) {
        return ImageTask.builder()
                .prompt(imageTaskInitiate.getPrompt())
                .lora(imageTaskInitiate.getLora())
                .status(imageTaskInitiate.getStatus())
                .creator(creator)
                .build();
    }

    public static ImageTaskRequest updatePrompt(ImageTaskRequest request, Network network) {
        String newPrompt = network.modifyPrompt(request.getPrompt(), request.getLora());
        String finalPrompt = (newPrompt == null || newPrompt.isBlank()) ? request.getPrompt() : newPrompt;
        return new ImageTaskRequest(request.getLora(), finalPrompt);
    }

    public static ImageQueueRequest toImageQueueRequest(ImageTaskRequest imageTaskRequest, Member creator) {
        return new ImageQueueRequest(imageTaskRequest.getLora(), imageTaskRequest.getPrompt(), creator.getId());
    }
}
