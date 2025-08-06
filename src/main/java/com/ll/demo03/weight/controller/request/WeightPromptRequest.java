package com.ll.demo03.weight.controller.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeightPromptRequest {
    private Long loraId;
    private String prompt;
}
