package com.ll.demo03.domain.adminImage.service;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminImageService {

    private final AdminImageRepository adminImageRepository;

    public AdminImage save(AdminImage adminImage) {
        return adminImageRepository.save(adminImage);
    }
    public AdminImage findById(Long id) {
        return adminImageRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public List<AdminImage> findAll() {
        return adminImageRepository.findAll();
    }

    public void delete(Long id) {
        adminImageRepository.deleteById(id);
    }
}