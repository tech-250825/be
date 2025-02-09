package com.ll.demo03.domain.adminImage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;                  // 이미지 ID
    private String key;               // R2에 저장된 파일 키
    private String url;               // 이미지 접근 URL (presigned)
    private String originalFileName;   // 원본 파일명
    private String contentType;       // 이미지 타입 (MIME)
    private Long fileSize;            // 파일 크기
    private String uploadedBy;        // 업로드한 관리자
    private LocalDateTime createdAt;  // 생성일시
    private LocalDateTime modifiedAt; // 수정일시
    private boolean isPublic;         // 공개 여부
    private String status;            // 상태 (ACTIVE, DELETED 등)

    // 파일 크기를 읽기 쉬운 형태로 변환 (1.5 MB 등)
    public String getFormattedFileSize() {
        if (fileSize == null) return "0";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));

        return String.format("%.1f %s",
                fileSize / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    // 파일 확장자 추출
    public String getFileExtension() {
        if (originalFileName == null) return "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : originalFileName.substring(lastDotIndex + 1);
    }

    // 이미지 파일 여부 확인
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
}
