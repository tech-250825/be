package com.ll.demo03.domain.imageTask.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageMessageRequest {
    private String lora;
    private String prompt;
    private Long memberId;
}