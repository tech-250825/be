package com.ll.demo03.lora.repository;


import com.ll.demo03.lora.entity.Lora;
import com.ll.demo03.lora.entity.MediaType;
import com.ll.demo03.lora.entity.StyleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoraRepository extends JpaRepository<Lora, Long> {
        List<Lora> findByMediaTypeAndStyleType(MediaType mediaType, StyleType styleType);


}

