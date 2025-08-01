package com.ll.demo03.global.port;

public interface Network {

    String createImage(Long taskId, String lora, String prompt, int width, int height, String webhook);
    String createT2VVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames, String webhook);
    String createI2VVideo(Long taskId, String prompt, String url, int width, int height, int numFrames, String webhook);
    String modifyPrompt(String lora, String prompt);

}
