package com.ll.demo03.lora.service.port;

import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;

import java.util.List;
import java.util.Optional;

public interface LoraRepository {
    List<Lora> findAllByMediaTypeAndStyleType(MediaType mediaType, StyleType styleType);

    Optional<String> findTriggerWordById(Long id);

    Optional<String> findPromptById(Long id);

    Optional<Lora> findById(Long id);
}
