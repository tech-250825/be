package com.ll.demo03.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;

public class JsonParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT);
        }
    }

}