package com.ll.demo03.weight.service;


import com.ll.demo03.global.port.Network;
import com.ll.demo03.weight.controller.port.WeightService;
import com.ll.demo03.weight.controller.request.WeightPromptRequest;
import com.ll.demo03.weight.controller.response.WeightResponse;
import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.weight.service.port.WeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeightServiceImpl implements WeightService {

    private final WeightRepository weightRepository;
    private final Network network;

    @Override
    public List<WeightResponse> get(MediaType mediaType, StyleType styleType, ModelType modelType) {
        List<Weight> lists = weightRepository.findAllByMediaTypeAndStyleTypeAndModelType(mediaType, styleType, modelType);
        return lists.stream()
                .map(WeightResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public String updatePrompt(WeightPromptRequest request) {
        String gptPrompt = weightRepository.findPromptById(request.getLoraId())
                .filter(prompt -> !prompt.isBlank())
                .orElse("If the user's input is in Korean, translate it into natural English. " +
                        "If the input is already in English, do not change it. " +
                        "Do not add any style or artistic interpretation — just translate or preserve as is.");

        return network.modifyPrompt(gptPrompt, request.getPrompt());
    }

    @Override
    public String updatePrompt(Long id, String oldPrompt) {
        String gptPrompt = weightRepository.findPromptById(id)
                .filter(prompt -> !prompt.isBlank())
                .orElse("If the user's input is in Korean, translate it into natural English. " +
                        "If the input is already in English, do not change it. " +
                        "Do not add any style or artistic interpretation — just translate or preserve as is.");

        return network.modifyPrompt(gptPrompt, oldPrompt);
    }

    @Override
    public String updatePrompt(String oldPrompt) {
        return network.modifyPrompt("If the user's input is in Korean, translate it into natural English. " +
                "If the input is already in English, do not change it. " +
                "Do not add any style or artistic interpretation — just translate or preserve as is." , oldPrompt);
    }

    @Override
    public String addTriggerWord(Long id, String oldPrompt) {
        return weightRepository.findTriggerWordById(id)
                .filter(word -> !word.isBlank())
                .map(word -> word + " " + oldPrompt)
                .orElse(oldPrompt);
    }
}

