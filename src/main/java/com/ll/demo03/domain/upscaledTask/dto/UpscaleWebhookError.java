package com.ll.demo03.domain.upscaledTask.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpscaleWebhookError {
    private int code;
    private String raw_message;
    private String message;
    private String detail;
}
