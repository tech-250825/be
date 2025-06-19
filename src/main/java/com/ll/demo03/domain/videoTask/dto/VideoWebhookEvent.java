package com.ll.demo03.domain.videoTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
public class VideoWebhookEvent {
    private int code;
    private VideoTaskData data;
    private String message;
    private long timestamp;

    @Data
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
        private VideoTaskMeta meta;
        private Object detail;
        private List<Object> logs;
        private VideoTaskError error;
    }

    @Data
    public static class VideoTaskConfig {
        @JsonProperty("service_mode")
        private String serviceMode;
        @JsonProperty("webhook_config")
        private WebhookConfig webhookConfig;

        @Data
        public static class WebhookConfig {
            private String endpoint;
            private String secret;
        }
    }

    @Data
    public static class VideoTaskInput {
        @JsonProperty("expand_prompt")
        private boolean expandPrompt;

        @JsonProperty("image_url")
        private String imageUrl;

        private String model;
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
        public static class CameraControl {
            private String type;
            private Config config;

            @Data
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
    public static class VideoTaskOutput {
        private int percent;
        @JsonProperty("cover_url")
        private String coverUrl;
        @JsonProperty("video_url")
        private String videoUrl;
        @JsonProperty("download_url")
        private String downloadUrl;
        private String desc;
        @JsonProperty("prompt_img_url")
        private String promptImgUrl;
        private int status;
        @JsonProperty("user_origin_prompt")
        private boolean userOriginPrompt;
        private int width;
        private int height;
        @JsonProperty("model_id")
        private String modelId;
        private String message;
    }

    @Data
    public static class VideoTaskMeta {
        @JsonProperty("created_at")
        private ZonedDateTime createdAt;
        @JsonProperty("started_at")
        private ZonedDateTime startedAt;
        @JsonProperty("ended_at")
        private ZonedDateTime endedAt;
        private Usage usage;
        @JsonProperty("is_using_private_pool")
        private boolean isUsingPrivatePool;

        @Data
        public static class Usage {
            private String type;
            private long frozen;
            private long consume;
        }
    }

    @Data
    public static class VideoTaskError {
        private int code;
        @JsonProperty("raw_message")
        private String rawMessage;
        private String message;
        private Object detail;
    }
}
