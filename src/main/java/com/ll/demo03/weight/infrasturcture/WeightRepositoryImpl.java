package com.ll.demo03.weight.infrasturcture;

import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.weight.service.port.WeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class WeightRepositoryImpl implements WeightRepository {

    private final WeightJpaRepository weightJpaRepository;

    @Override
    public List<Weight> findAllByMediaTypeAndStyleTypeAndModelType(MediaType mediaType, StyleType styleType, ModelType modelType) {
        return weightJpaRepository.findAllByMediaTypeAndStyleTypeAndModelType(mediaType, styleType, modelType)
                .stream()
                .map(WeightEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> findTriggerWordById(Long id){
        return weightJpaRepository.findTriggerWordById(id);
    }

    @Override
    public Optional<String> findPromptById(Long id){
        return weightJpaRepository.findPromptById(id);
    }

    @Override
    public Optional<Weight> findById(Long id){
        return weightJpaRepository.findById(id).map(WeightEntity::toModel);
    }

}