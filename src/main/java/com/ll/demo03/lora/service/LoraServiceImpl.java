package com.ll.demo03.lora.service;


import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.lora.controller.port.LoraService;
import com.ll.demo03.lora.controller.request.LoraPromptRequest;
import com.ll.demo03.lora.controller.response.LoraResponse;
import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.infrasturcture.LoraEntity;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;
import com.ll.demo03.lora.infrasturcture.LoraJpaRepository;
import com.ll.demo03.lora.service.port.LoraRepository;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoraServiceImpl implements LoraService {

    private final LoraRepository loraRepository;
    private final Network network;

    @Override
    public List<LoraResponse> getLora(MediaType mediaType, StyleType styleType) {
        List<Lora> lists = loraRepository.findAllByMediaTypeAndStyleType(mediaType, styleType);
        return lists.stream()
                .map(LoraResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public String updatePrompt(LoraPromptRequest request) {
        String gptPrompt = loraRepository.findPromptById(request.getLoraId())
                .filter(prompt -> !prompt.isBlank())
                .orElse("If the user's input is in Korean, translate it into natural English. " +
                        "If the input is already in English, do not change it. " +
                        "Do not add any style or artistic interpretation — just translate or preserve as is.");

        return network.modifyPrompt(gptPrompt, request.getPrompt());
    }

    @Override
    public String addTriggerWord(Long id, String oldPrompt) {
        return loraRepository.findTriggerWordById(id)
                .filter(word -> !word.isBlank())
                .map(word -> word + " " + oldPrompt)
                .orElse(oldPrompt);
    }
}

