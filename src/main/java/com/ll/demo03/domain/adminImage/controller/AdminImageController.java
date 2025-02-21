package com.ll.demo03.domain.adminImage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.adminImage.dto.AdminImageRequest;
import com.ll.demo03.domain.adminImage.dto.AdminImageResponse;
import com.ll.demo03.domain.adminImage.dto.FileResponse;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.service.AdminImageService;
import com.ll.demo03.domain.adminImage.service.FileService;
import com.ll.demo03.domain.imageCategory.service.CategoryService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class AdminImageController {
    private final FileService fileService;
    private final CategoryService categoryService;
    private final AdminImageService adminImageService;

    //cloudflare에 이미지 파일 업로드하여 url 반환
    @PostMapping("/admin/image/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("metadata") String metadata) {
        try {
            // JSON 문자열을 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            AdminImageRequest adminImageRequest = objectMapper.readValue(metadata, AdminImageRequest.class);

            System.out.println("Hashtags: " + adminImageRequest.getHashtags());

            AdminImage adminImage = fileService.uploadFile(file, adminImageRequest);
            AdminImageResponse adminImageResponse = AdminImageResponse.from(adminImage);
            return ResponseEntity.ok(Map.of(
                    "message", "파일 업로드 성공",
                    "image", adminImageResponse
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // cloudflare에 있는 이미지 파일 조회
    @GetMapping("/admin/image/files")
    public ResponseEntity<List<FileResponse>> listFiles() {
        try {
            List<FileResponse> files = fileService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

//    @PostMapping("/admin/image")
//    public GlobalResponse<String> createImage(
//            @RequestBody AdminImageRequest request) {
//        ImageCategory category = categoryService.findSubCategory(
//                request.getMainCategoryId(),
//                request.getSubCategoryId()
//        );
//
//        AdminImage savedImage = adminImageService.save(request);
//        return GlobalResponse.success("이미지 등록 성공");
//    }

    @DeleteMapping("/admin/image/{adminImageId}")
    public GlobalResponse<String> deleteImage(@PathVariable Long adminImageId) {
        try {
            adminImageService.delete(adminImageId);
            return GlobalResponse.success("이미지 삭제에 성공했습니다.");
        } catch (EntityNotFoundException e) {
            return GlobalResponse.error(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    @GetMapping("/home")
    public GlobalResponse<Page<AdminImageResponse>> getImagesByCategory(
            @RequestParam(name = "maintag", required = false) Long mainCategoryId,
            @RequestParam(name = "subtag", required = false) Long subCategoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection) {

        Sort sorting = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<AdminImage> images;
        if (subCategoryId != null) {
            images = adminImageService.findBySubCategory(subCategoryId, pageable);
        } else if (mainCategoryId != null) {
            images = adminImageService.findByMainCategory(mainCategoryId, pageable);
        } else {
            images = adminImageService.findAll(pageable);
        }

        Page<AdminImageResponse> responses = images.map(AdminImageResponse::from);
        return GlobalResponse.success(responses);
    }
}
