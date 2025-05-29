package com.ll.demo03.domain.upscaledTask.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class UpscaleTaskRequestMessage implements Serializable {
    private String taskId;
    private String index;
    private Long memberId;
    private String webhookUrl;
}
