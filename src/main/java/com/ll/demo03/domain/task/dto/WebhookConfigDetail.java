package com.ll.demo03.domain.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookConfigDetail {
    private String endpoint;
    private String secret;
}
