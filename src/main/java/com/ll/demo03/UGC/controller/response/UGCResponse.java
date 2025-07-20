package com.ll.demo03.UGC.controller.response;

import com.ll.demo03.UGC.domain.UGC;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class UGCResponse {
    private Long id;
    private String url;
    private int index;
    private LocalDateTime createdAt;

    public static UGCResponse of(UGC UGC) {
        return new UGCResponse(
                UGC.getId(),
                UGC.getUrl(),
                UGC.getIndex(),
                UGC.getCreatedAt()
        );
    }
}
