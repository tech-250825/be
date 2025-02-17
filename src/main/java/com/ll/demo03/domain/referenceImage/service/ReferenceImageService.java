package com.ll.demo03.domain.referenceImage.service;

import com.ll.demo03.domain.adminImage.dto.AdminImageRequest;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
import com.ll.demo03.domain.hashtag.service.HashtagService;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
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
public class ReferenceImageService {
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        int randomNum = new Random().nextInt(100000);
        String randomFileName = timestamp + "_" + randomNum + extension;

        String fileKey = "reference-images/" + randomFileName;

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
}