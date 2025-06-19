package com.ll.demo03.domain.videoTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoWebhookEvent {
    private int code;
    private String message;
    private VideoTaskData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskData {
        @JsonProperty("task_id")
        private String taskId;

        private String model;

        @JsonProperty("task_type")
        private String taskType;

        private String status;

        private VideoTaskConfig config;

        private VideoTaskInput input;

        private VideoTaskOutput output;

        private Map<String, Object> meta;

        private Object detail;

        private List<Object> logs;

        private VideoTaskError error;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskConfig {
        @JsonProperty("service_mode")
        private String serviceMode;

        @JsonProperty("webhook_config")
        private WebhookConfig webhookConfig;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class WebhookConfig {
            private String endpoint;
            private String secret;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskInput {
        private String prompt;

        @JsonProperty("negative_prompt")
        private String negativePrompt;

        @JsonProperty("cfg_scale")
        private double cfgScale;

        private int duration;

        @JsonProperty("aspect_ratio")
        private String aspectRatio;

        private String mode;

        @JsonProperty("camera_control")
        private CameraControl cameraControl;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CameraControl {
            private String type;
            private Config config;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Config {
                private int horizontal;
                private int vertical;
                private int pan;
                private int tilt;
                private int roll;
                private int zoom;
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskOutput {
        @JsonProperty("video_url")
        private String videoUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskError {
        private int code;

        @JsonProperty("raw_message")
        private String rawMessage;

        private String message;

        private Object detail;
    }
}
