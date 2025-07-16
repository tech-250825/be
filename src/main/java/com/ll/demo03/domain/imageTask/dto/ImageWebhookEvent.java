package com.ll.demo03.domain.imageTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageWebhookEvent {

    @JsonProperty("delayTime")
    private Long delayTime;

    @JsonProperty("executionTime")
    private Long executionTime;

    @JsonProperty("id")
    private String runpodId;

    private ImageTaskInput input;

    private ImageTaskOutput output;

    private String status;

    private String webhook;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageTaskInput {
        private com.ll.demo03.domain.videoTask.dto.VideoWebhookEvent.VideoTaskInput.Payload payload;
        private String workflow;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Payload {
            private String lora;

            @JsonProperty("positive_prompt")
            private String positivePrompt;

            @JsonProperty("task_id")
            private Long taskId;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageTaskOutput {
        private String images;
    }
}