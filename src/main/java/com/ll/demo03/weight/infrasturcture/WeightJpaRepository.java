package com.ll.demo03.weight.infrasturcture;


import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeightJpaRepository extends JpaRepository<WeightEntity, Long> {

    List<WeightEntity> findAllByMediaTypeAndStyleTypeAndModelType(MediaType mediaType, StyleType styleType, ModelType modelType);
    @Query("SELECT l.triggerWord FROM WeightEntity l WHERE l.id = :id")
    Optional<String> findTriggerWordById(Long id);
    @Query("SELECT e.prompt FROM WeightEntity e WHERE e.id = :id")
    Optional<String> findPromptById(Long id);
    Optional<WeightEntity> findById(Long id);
}

