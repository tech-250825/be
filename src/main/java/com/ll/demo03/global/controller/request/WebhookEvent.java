package com.ll.demo03.global.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    @JsonProperty("delayTime")
    private Long delayTime;

    @JsonProperty("executionTime")
    private Long executionTime;

    @JsonProperty("id")
    private String runpodId;

    private TaskInput input;

    private TaskOutput output;

    private String status;

    private String webhook;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskInput {
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
    public static class TaskOutput {
        private String images;
    }

    public String getImages() {
        return this.output != null ? this.output.getImages() : null;
    }

    public Long getTaskId() {
        return this.input != null && this.input.getPayload() != null
                ? this.input.getPayload().getTaskId()
                : null;
    }

    public String getPrompt() {
        return this.input != null && this.input.getPayload() != null
                ? this.input.getPayload().getPositivePrompt()
                : null;
    }

    public String getRunpodId() {
        return this.runpodId;
    }
}