package com.ll.demo03.domain.sharedImage.controller;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.domain.sharedImage.entity.SharedImage;
import com.ll.demo03.domain.sharedImage.service.SharedImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shared-images")
public class SharedImageController {

    private final SharedImageService sharedImageService;

    @Autowired
    public SharedImageController(SharedImageService sharedImageService) {
        this.sharedImageService = sharedImageService;
    }


    @GetMapping
    public ResponseEntity<List<SharedImage>> getAllSharedImages() {
        List<SharedImage> sharedImages = sharedImageService.getAllSharedImages();
        return ResponseEntity.ok(sharedImages);
    }


    @GetMapping("/mine")
    public ResponseEntity<List<SharedImage>> getMySharedImages(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        List<SharedImage> mySharedImages = sharedImageService.getMySharedImages(member.getId());
        return ResponseEntity.ok(mySharedImages);
    }


    @GetMapping("/{id}")
    public ResponseEntity<SharedImage> getSharedImageById(@PathVariable Long id) {
        return sharedImageService.getSharedImageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<SharedImage> createSharedImage(@RequestParam Long imageId) {
        try {
            SharedImage createdSharedImage = sharedImageService.createSharedImage(imageId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSharedImage);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSharedImage(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ) {
        Member member = principalDetails.user();
        try {
            boolean deleted = sharedImageService.deleteSharedImage(id, member.getId());
            return deleted ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
