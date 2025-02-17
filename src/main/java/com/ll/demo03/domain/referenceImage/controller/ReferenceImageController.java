package com.ll.demo03.domain.referenceImage.controller;

import com.ll.demo03.domain.adminImage.dto.AdminImageResponse;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.referenceImage.service.ReferenceImageService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/api/reference")
public class ReferenceImageController {
    private final ReferenceImageService referenceImageService;

    @PostMapping("/image")
    public GlobalResponse<String> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String url=referenceImageService.uploadFile(file);
            return GlobalResponse.success(url);
        } catch (IOException e) {
            return GlobalResponse.error(ErrorCode.DATABASE_ERROR);
        }
    }
}
