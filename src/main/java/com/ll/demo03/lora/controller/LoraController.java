package com.ll.demo03.lora.controller;


import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.lora.controller.port.LoraService;
import com.ll.demo03.lora.controller.request.LoraPromptRequest;
import com.ll.demo03.lora.controller.response.LoraResponse;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public GlobalResponse<String> updatePrompt(@RequestBody LoraPromptRequest request){
        return GlobalResponse.success(loraService.updatePrompt(request));
    }

}

