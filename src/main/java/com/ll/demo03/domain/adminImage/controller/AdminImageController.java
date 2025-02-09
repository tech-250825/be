package com.ll.demo03.domain.adminImage.controller;

import com.ll.demo03.domain.adminImage.dto.FileResponse;
import com.ll.demo03.domain.adminImage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminImageController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {
        try {
            fileService.uploadFile(file, title);
            return ResponseEntity.ok(Map.of(
                    "message", "파일 업로드 성공",
                    "filename", file.getOriginalFilename()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "파일 업로드 실패"));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> listFiles() {
        try {
            List<FileResponse> files = fileService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
