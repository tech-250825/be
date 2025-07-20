package com.ll.demo03.lora.controller;


import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.lora.dto.LoraResponse;
import com.ll.demo03.lora.entity.MediaType;
import com.ll.demo03.lora.entity.StyleType;
import com.ll.demo03.lora.service.LoraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lora")
public class LoraController {

    private final LoraService loraService;

    @GetMapping
    public GlobalResponse<List<LoraResponse>> getLoras(
            @RequestParam MediaType mediaType,
            @RequestParam StyleType styleType
    ) {
        return GlobalResponse.success(loraService.getLora(mediaType, styleType));
    }

}

