package com.ll.demo03.domain.adminImage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String key;               // R2에 저장된 파일 키
    private String url;               // 이미지 접근 URL (presigned)     // 상태 (ACTIVE, DELETED 등)
}
