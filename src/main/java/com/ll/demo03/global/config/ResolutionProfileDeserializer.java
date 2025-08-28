package com.ll.demo03.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ll.demo03.global.domain.ResolutionProfile;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;

import java.io.IOException;

public class ResolutionProfileDeserializer extends JsonDeserializer<ResolutionProfile> {
    
    @Override
    public ResolutionProfile deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();
        
        try {
            return ResolutionProfile.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (value.contains(":")) {
                String[] parts = value.split(":");
                if (parts.length >= 2) {
                    String ratio = parts[0] + ":" + parts[1];
                    boolean isHD = parts.length > 2 && "HD".equalsIgnoreCase(parts[2]);
                    return ResolutionProfile.from(ratio, isHD);
                }
            }
            
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }
    }
}