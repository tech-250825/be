package com.ll.demo03.lora.service;


import com.ll.demo03.lora.dto.LoraResponse;
import com.ll.demo03.lora.entity.Lora;
import com.ll.demo03.lora.entity.MediaType;
import com.ll.demo03.lora.entity.StyleType;
import com.ll.demo03.lora.repository.LoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoraService {

    private final LoraRepository loraRepository;

    public List<LoraResponse> getLora(MediaType mediaType, StyleType styleType) {
        List<Lora> lists = loraRepository.findByMediaTypeAndStyleType(mediaType, styleType);
        return lists.stream()
                .map(LoraResponse::from)
                .collect(Collectors.toList());
    }

}

