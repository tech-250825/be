package com.ll.demo03.global.port;

public interface Network {

    String createImage(Long taskId, String lora, String prompt, String webhook);
    String createVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames, String webhook);
    String modifyPrompt(String lora, String prompt);

}
