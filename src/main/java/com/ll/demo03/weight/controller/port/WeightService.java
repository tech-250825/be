package com.ll.demo03.weight.controller.port;

import com.ll.demo03.weight.controller.request.WeightPromptRequest;
import com.ll.demo03.weight.controller.response.WeightResponse;
import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;

import java.util.List;

public interface WeightService {

    List<WeightResponse> get(MediaType mediaType, StyleType styleType, ModelType modelType);

    String addTriggerWord(Long id, String prompt);

    String updatePrompt(WeightPromptRequest request);
}
