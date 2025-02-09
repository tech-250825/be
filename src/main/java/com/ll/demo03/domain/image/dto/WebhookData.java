package com.ll.demo03.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class WebhookData {
    private String task_id;
    private String model;
    private String task_type;
    private String status;
    private WebhookConfig config;
    private WebhookInput input;
    private WebhookOutput output;
    private WebhookMeta meta;
    private String detail;
    private List<String> logs;
    private WebhookError error;
}
