package com.ll.demo03.mock;

import com.ll.demo03.weight.controller.port.WeightService;
import com.ll.demo03.weight.controller.request.WeightPromptRequest;
import com.ll.demo03.weight.controller.response.WeightResponse;
import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;

import java.util.List;

public class FakeWeightService implements WeightService {

    @Override
    public String addTriggerWord(Long weightId, String prompt) {
        return "[FAKE_MODIFIED] " + prompt;
    }

    @Override
    public String updatePrompt(WeightPromptRequest request) {
        return "[FAKE_UPDATED] " + request.getPrompt();
    }

    @Override
    public String updatePrompt(String prompt) {
        return "[FAKE_UPDATED] " + prompt;
    }

    @Override
    public String updatePrompt(Long id, String prompt) {
        return "[FAKE_UPDATED] " + prompt;
    }


    @Override
    public List<WeightResponse> get(MediaType mediaType, StyleType styleType, ModelType modelType) {
        return List.of();
    }
}
