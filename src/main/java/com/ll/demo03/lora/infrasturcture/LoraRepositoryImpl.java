package com.ll.demo03.lora.infrasturcture;

import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;
import com.ll.demo03.lora.service.port.LoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class LoraRepositoryImpl implements LoraRepository {

    private final LoraJpaRepository loraJpaRepository;

    @Override
    public List<Lora> findAllByMediaTypeAndStyleType(MediaType mediaType, StyleType styleType) {
        return loraJpaRepository.findAllByMediaTypeAndStyleType(mediaType, styleType)
                .stream()
                .map(LoraEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> findTriggerWordById(Long id){
        return loraJpaRepository.findTriggerWordById(id);
    }

    @Override
    public Optional<String> findPromptById(Long id){
        return loraJpaRepository.findPromptById(id);
    }

    @Override
    public Optional<Lora> findById(Long id){
        return loraJpaRepository.findById(id).map(LoraEntity::toModel);
    }

}