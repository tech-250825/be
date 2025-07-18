package com.ll.demo03.imageTask.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageTaskInitiate {
    private final String prompt;
    private final String lora;
    private final String status;
    private final Long creatorId;

    @Builder
    public ImageTaskInitiate(String prompt, String lora, String status, Long creatorId) {
        this.prompt = prompt;
        this.lora = lora;
        this.status = status;
        this.creatorId = creatorId;
    }
}
