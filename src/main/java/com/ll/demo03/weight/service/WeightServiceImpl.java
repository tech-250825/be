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
    public String addTriggerWord(Long id, String oldPrompt) {
        return weightRepository.findTriggerWordById(id)
                .filter(word -> !word.isBlank())
                .map(word -> word + " " + oldPrompt)
                .orElse(oldPrompt);
    }
}

