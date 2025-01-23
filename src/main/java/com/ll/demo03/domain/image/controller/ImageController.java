package com.ll.demo03.domain.image.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.service.ImageService;
import com.ll.demo03.global.dto.GlobalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mashape.unirest.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/create")
    public GlobalResponse<String> createImage(@RequestBody Map<String, String> data) {
        // GPT 응답을 받아옴
        String gptResponse = imageService.sendToGpt(data);

        // JSON 파싱 ``
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(gptResponse);
            String content = jsonNode.get("choices").get(0).get("message").get("content").asText();

            // 이미지 생성
            String taskId = imageService.createImage(content, data.get("ratio"));

            return GlobalResponse.success(taskId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    @GetMapping("/status/{taskId}")
    public GlobalResponse<String> checkStatus(@PathVariable String taskId) {
        String imageUrl = imageService.checkTaskStatus(taskId);
        return GlobalResponse.success(imageUrl);
    }

    @PostMapping("/upscale")
    public GlobalResponse<String> upscaleImage(@RequestBody Map<String, String> data) {
        String taskId = imageService.upscale(data.get("originTaskId"), Integer.parseInt(data.get("index")));
        String upscaledImageUrl = imageService.checkTaskStatus(taskId);
        return GlobalResponse.success(upscaledImageUrl);
    }
}