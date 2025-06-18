package com.ll.demo03.domain.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.time.Instant;
import java.util.Random;

@Transactional
@Service
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class ProfileImageService {
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    // 프로필 이미지 업로드 메서드 추가
    public String uploadProfileImage(MultipartFile file, Long memberId) throws IOException {
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (!isValidImageFile(originalFilename)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        int randomNum = new Random().nextInt(100000);
        String randomFileName = timestamp + "_" + randomNum + extension;

        // profile-images 폴더에 저장
        String fileKey = "profile-images/" + memberId + "/" + randomFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = "https://image.hoit.my/" + fileKey;
        return fileUrl;
    }

    private boolean isValidImageFile(String filename) {
        if (filename == null) return false;
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") ||
                lowerCaseFilename.endsWith(".jpeg") ||
                lowerCaseFilename.endsWith(".png") ||
                lowerCaseFilename.endsWith(".gif") ||
                lowerCaseFilename.endsWith(".webp");
    }
}
