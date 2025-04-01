package com.ll.demo03.domain.referenceImage.controller;

import com.ll.demo03.domain.referenceImage.dto.ReferenceImageResponse;
import com.ll.demo03.domain.referenceImage.service.ReferenceImageService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


import java.io.ByteArrayInputStream;
import java.io.File;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/reference")
public class ReferenceImageController {
    private final ReferenceImageService referenceImageService;

    @PostMapping("/images")
    public GlobalResponse<ReferenceImageResponse> uploadFileForImage(
            @RequestParam(required = false) String crefImageUrl,
            @RequestParam(required = false) MultipartFile crefImage,
            @RequestParam(required = false) String srefImageUrl,
            @RequestParam(required = false) MultipartFile srefImage) {
        try {
            String crefUrl = null;
            String srefUrl = null;

            if (crefImageUrl != null) {
                crefUrl = uploadImageFromUrl(crefImageUrl);
            }
            else if (crefImage != null) {
                crefUrl = referenceImageService.uploadFile(crefImage);
            }


            if (srefImageUrl != null) {
                srefUrl = uploadImageFromUrl(srefImageUrl);
            }
            else if (srefImage != null) {
                srefUrl = referenceImageService.uploadFile(srefImage);
            }

            ReferenceImageResponse response = new ReferenceImageResponse(crefUrl, srefUrl);

            return GlobalResponse.success(response);
        } catch (IOException e) {
            return GlobalResponse.error(ErrorCode.DATABASE_ERROR);
        }
    }

    private String uploadImageFromUrl(String imageUrl) throws IOException {
        byte[] imageData = downloadImage(imageUrl);
        MultipartFile multipartFile = convertToMultipartFile(imageData);
        return referenceImageService.uploadFile(multipartFile);
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    private MultipartFile convertToMultipartFile(byte[] imageData) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return "image.png";
            }

            @Override
            public String getContentType() {
                return "image/png";
            }

            @Override
            public boolean isEmpty() {
                return imageData.length == 0;
            }

            @Override
            public long getSize() {
                return imageData.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return imageData;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(imageData);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
            }
        };
    }



    @PostMapping("/videos")
    public GlobalResponse<String> uploadFileForVideo(
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String url = null;
            if (imageUrl != null) {
                url = uploadImageFromUrl(imageUrl);
            }
            else if (image != null) {
                url = referenceImageService.uploadFile(image);
            }
            return GlobalResponse.success(url);
        } catch (IOException e) {
            return GlobalResponse.error(ErrorCode.DATABASE_ERROR);
        }
    }

}
