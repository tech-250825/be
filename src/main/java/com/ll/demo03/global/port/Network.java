package com.ll.demo03.global.port;

public interface Network {

    String createImageFaceDetailer(Long taskId, String checkpoint, String lora, String prompt, String negativePrompt, int width, int height, String webhook);
    String createImagePlain (Long taskId, String checkpoint, String prompt, String negativePrompt, int width, int height, String webhook);
    String createI2I(Long taskId, String prompt, String imageUrl, String webhook);
    String createT2VVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames, String webhook);
    String createI2VVideo(Long taskId, String prompt, String url, int width, int height, int numFrames, String webhook);

}
