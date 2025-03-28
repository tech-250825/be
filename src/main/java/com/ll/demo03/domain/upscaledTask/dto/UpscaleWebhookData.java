package com.ll.demo03.domain.upscaledTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ll.demo03.domain.task.dto.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpscaleWebhookData {
    private String task_id;
    private String model;
    private String task_type;
    private String status;
    private UpscaleWebhookConfig config;
    private UpscaleWebhookInput input;
    private UpscaleWebhookOutput output;
    private UpscaleWebhookMeta meta;
    private String detail;
    private List<String> logs;
    private UpscaleWebhookError error;
}

