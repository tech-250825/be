package com.ll.demo03.mock;

import com.ll.demo03.weight.domain.*;
import com.ll.demo03.weight.service.port.WeightRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeWeightRepository implements WeightRepository {

    private final Map<Long, Weight> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Optional<Weight> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Weight> findAllByMediaTypeAndStyleTypeAndModelType(MediaType mediaType, StyleType styleType, ModelType modelType) {
        return storage.values().stream()
                .filter(weight -> Objects.equals(weight.getMediaType(), mediaType) &&
                                Objects.equals(weight.getStyleType(), styleType) &&
                                Objects.equals(weight.getModelType(), modelType))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> findTriggerWordById(Long id) {
        return Optional.ofNullable(storage.get(id))
                .map(Weight::getTriggerWord);
    }

    @Override
    public Optional<String> findPromptById(Long id) {
        return Optional.ofNullable(storage.get(id))
                .map(Weight::getPrompt);
    }

    // Helper method to add test data
    public void addTestWeight(Long id, String modelName, String triggerWord) {
        Weight weight = Weight.builder()
                .id(id)
                .modelName(modelName)
                .triggerWord(triggerWord)
                .build();
        storage.put(id, weight);
    }
}