package com.ll.demo03.domain.adminImage.service;

import com.ll.demo03.domain.adminImage.dto.AdminImageRequest;
import com.ll.demo03.domain.adminImage.dto.FileResponse;
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
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Transactional
@Service
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AdminImageRepository adminImageRepository;
    private final CategoryService categoryService;
    private final HashtagService hashtagService;

    @Value("${r2.bucket}")
    private String bucket;

    public AdminImage uploadFile(MultipartFile file, AdminImageRequest adminImageRequest) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        int randomNum = new Random().nextInt(100000);
        String randomFileName = timestamp + "_" + randomNum + extension;

        String fileKey = "images/" + randomFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        String fileUrl = "https://hoit.my/" + fileKey;

        AdminImage adminImage = adminImageRequest.toEntity(categoryService, fileUrl);
        AdminImage savedImage = adminImageRepository.save(adminImage);
        hashtagService.createHashtags(savedImage, adminImageRequest.getHashtags());
        return savedImage;
    }

    public List<FileResponse> listFiles() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        List<FileResponse> files = new ArrayList<>();

        for (S3Object s3Object : listResponse.contents()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Object.key())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(presignRequest)
                    .url()
                    .toString();

            FileResponse fileResponse = new FileResponse();
            fileResponse.setKey(s3Object.key());
            fileResponse.setUrl(presignedUrl);
            files.add(fileResponse);
        }

        return files;
    }
}
