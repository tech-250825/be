package com.ll.demo03.lora.infrasturcture;


import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoraJpaRepository extends JpaRepository<LoraEntity, Long> {
        List<LoraEntity> findAllByMediaTypeAndStyleType(MediaType mediaType, StyleType styleType);

    @Query("SELECT l.triggerWord FROM LoraEntity l WHERE l.id = :id")
    Optional<String> findTriggerWordById(Long id);
    @Query("SELECT e.prompt FROM LoraEntity e WHERE e.id = :id")
    Optional<String> findPromptById(Long id);
    Optional<LoraEntity> findById(Long id);
}

