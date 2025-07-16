package com.ll.demo03.domain.imageTask.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageTaskRequest {
    private String lora;
    private String prompt;
}
