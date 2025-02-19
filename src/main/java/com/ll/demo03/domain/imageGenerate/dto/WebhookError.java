package com.ll.demo03.domain.imageGenerate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookError {
    private Integer code;
    private String raw_message;
    private String message;
    private String detail;
}
