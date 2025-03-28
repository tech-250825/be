package com.ll.demo03.domain.upscaledTask.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpscaleWebhookInput {
    private String index;
    private String origin_task_id;
}
