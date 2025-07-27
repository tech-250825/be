package com.ll.demo03.lora.controller.port;

import com.ll.demo03.lora.controller.request.LoraPromptRequest;
import com.ll.demo03.lora.controller.response.LoraResponse;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;

import java.util.List;

public interface LoraService {

    List<LoraResponse> getLora(MediaType mediaType, StyleType styleType);

    String addTriggerWord(Long id, String prompt);

    String updatePrompt(LoraPromptRequest request);
}
