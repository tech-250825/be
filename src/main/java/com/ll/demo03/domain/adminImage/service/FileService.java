package com.ll.demo03.domain.adminImage.service;

import com.ll.demo03.domain.adminImage.dto.FileResponse;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AdminImageRepository adminImageRepository;

    @Value("${r2.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String title) throws IOException {
        String fileKey = "images/" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))  // URL 유효기간 1시간
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build())
                .build();

        String fileUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

//        AdminImage adminImage = new AdminImage();
//        adminImage.setUrl(fileUrl);
//        adminImage.setPrompt(title);
//        adminImageRepository.save(adminImage);

        return fileUrl;
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
