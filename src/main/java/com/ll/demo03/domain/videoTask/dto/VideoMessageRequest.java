package com.ll.demo03.domain.videoTask.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoMessageRequest {
    private String lora;
    private String prompt;
    private Long memberId;
}