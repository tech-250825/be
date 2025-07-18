package com.ll.demo03.UGC.service;

import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.global.port.ResponseConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UGCResponseConverter implements ResponseConverter<UGC, UGCResponse> {

    @Override
    public List<UGCResponse> convertToResponses(List<UGC> entities) {
        return entities.stream()
                .map(UGCResponse::of)
                .collect(Collectors.toList());
    }
}