package com.ll.demo03.videoTask.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoWebhookEvent {

    @JsonProperty("delayTime")
    private Long delayTime;

    @JsonProperty("executionTime")
    private Long executionTime;

    @JsonProperty("id")
    private String runpodId;

    private VideoTaskInput input;

    private VideoTaskOutput output;

    private String status;

    private String webhook;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoTaskInput {
        private Payload payload;
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
    public static class VideoTaskOutput {
        private String images;
    }
}
