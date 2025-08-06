package com.ll.demo03.weight.service.port;

import com.ll.demo03.weight.domain.*;
import com.ll.demo03.weight.domain.Weight;

import java.util.List;
import java.util.Optional;

public interface WeightRepository {
    List<Weight> findAllByMediaTypeAndStyleTypeAndModelType(MediaType mediaType, StyleType styleType, ModelType modelType);

    Optional<String> findTriggerWordById(Long id);

    Optional<String> findPromptById(Long id);

    Optional<Weight> findById(Long id);
}
