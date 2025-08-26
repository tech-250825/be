package com.ll.demo03.mock;

import com.ll.demo03.global.port.Network;

import java.util.ArrayList;
import java.util.List;

public class FakeNetwork implements Network {

    @Override
    public String  createImageFaceDetailer(Long taskId, String checkpoint, String lora, String prompt,  String negativePrompt, int width, int height, String webhook){
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }

    @Override
    public String  createImagePlain(Long taskId, String checkpoint,  String prompt,  String negativePrompt, int width, int height, String webhook){
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }

    @Override
    public String createT2VVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames, String webhook) {
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }

    @Override
    public String createI2VVideo(Long taskId, String prompt, String url, int width, int height, int numFrames , String webhook) {
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }

    @Override
    public String createI2I(Long taskId, String prompt, String imageUrl, String webhook){
        return String.format("{\"status\":\"ok\",\"type\":\"img2img\",\"task_id\":%d}", taskId);
    }

}
