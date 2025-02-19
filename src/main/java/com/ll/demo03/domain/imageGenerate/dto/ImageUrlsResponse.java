package com.ll.demo03.domain.imageGenerate.dto;

import java.util.List;

// 이벤트 전송 객체
public class ImageUrlsResponse {
    private List<String> imageUrls;

    public ImageUrlsResponse(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
