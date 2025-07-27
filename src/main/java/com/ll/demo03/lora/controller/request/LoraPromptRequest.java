package com.ll.demo03.lora.controller.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoraPromptRequest {
    private Long loraId;
    private String prompt;
}
