package com.ll.demo03.domain.adminImage.controller;

import com.ll.demo03.domain.adminImage.dto.AdminImageRequest;
import com.ll.demo03.domain.adminImage.dto.AdminImageResponse;
import com.ll.demo03.domain.adminImage.dto.FileResponse;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.service.AdminImageService;
import com.ll.demo03.domain.adminImage.service.FileService;
import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin/image")
@RestController
@RequiredArgsConstructor
public class AdminImageController {
    private final FileService fileService;
    private final CategoryService categoryService;
    private final AdminImageService adminImageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {
        try {
            String url=fileService.uploadFile(file, title);
            return ResponseEntity.ok(Map.of(
                    "message", "파일 업로드 성공",
                    "url", url
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "파일 업로드 실패"));
        }
    }

    // cloudflare에 있는 이미지 파일 조회
    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> listFiles() {
        try {
            List<FileResponse> files = fileService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> createImage(
            @RequestBody AdminImageRequest request) {
        ImageCategory category = categoryService.findSubCategory(
                request.getMainCategoryId(),
                request.getSubCategoryId()
        );

        AdminImage adminImage = new AdminImage();
        adminImage.setUrl(request.getUrl());
        adminImage.setPrompt(request.getPrompt());
        adminImage.setCategory(category);

        AdminImage savedImage = adminImageService.save(adminImage);
        return ResponseEntity.ok("이미지 등록 성공");
    }
}
