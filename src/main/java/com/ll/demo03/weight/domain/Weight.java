package com.ll.demo03.weight.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Weight {

    private final Long id;
    private final String name;
    private final MediaType mediaType;
    private final StyleType styleType;
    private final ModelType modelType;
    private final String image;
    private final String modelName;
    private final String triggerWord;
    private final String prompt;

    @Builder
    public Weight(Long id, String name, MediaType mediaType, StyleType styleType, ModelType modelType, String image, String modelName, String triggerWord, String prompt) {
        this.id = id;
        this.name = name;
        this.mediaType = mediaType;
        this.styleType = styleType;
        this.modelType = modelType;
        this.image = image;
        this.modelName = modelName;
        this.triggerWord = triggerWord;
        this.prompt = prompt;
    }
}
