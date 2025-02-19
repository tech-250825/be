package com.ll.demo03.domain.imageGenerate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookEvent {
    private Long timestamp;
    private WebhookData data;
}

