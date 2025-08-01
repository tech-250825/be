package com.ll.demo03.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.notification.controller.response.BatchNotificationMessage;
import com.ll.demo03.notification.controller.response.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMessageFactory {
    private final ObjectMapper objectMapper;

    public Object parse(String json) throws CustomException, JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String type = root.get("type").asText();

        return switch (type) {
            case "video" -> objectMapper.treeToValue(root, NotificationMessage.class);
            case "image" -> objectMapper.treeToValue(root, BatchNotificationMessage.class);
            default -> throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        };
    }
}
