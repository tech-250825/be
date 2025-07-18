package com.ll.demo03.imageTask.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageQueueRequest {
    private String lora;
    private String prompt;
    private Long memberId;
}