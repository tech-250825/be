package com.ll.demo03.weight.controller;


import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.weight.controller.port.WeightService;
import com.ll.demo03.weight.controller.request.WeightPromptRequest;
import com.ll.demo03.weight.controller.response.WeightResponse;
import com.ll.demo03.weight.domain.MediaType;
import com.ll.demo03.weight.domain.ModelType;
import com.ll.demo03.weight.domain.StyleType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weights")
public class WeightController {

    private final WeightService weightService;

    @GetMapping
    public GlobalResponse<List<WeightResponse>> getLoras(
            @RequestParam MediaType mediaType,
            @RequestParam StyleType styleType,
            @RequestParam ModelType modelType
    ) {
        return GlobalResponse.success(weightService.get(mediaType, styleType, modelType));
    }

    @PostMapping
    public GlobalResponse<String> updatePrompt(@RequestBody WeightPromptRequest request){
        return GlobalResponse.success(weightService.updatePrompt(request));
    }

}

