package com.ll.demo03.mock;

import com.ll.demo03.global.port.Network;

import java.util.ArrayList;
import java.util.List;

public class FakeNetwork implements Network {

    public static class ImageCall {
        public final Long taskId;
        public final String checkpoint;
        public final String lora;
        public final String prompt;
        public final int width;
        public final int height;
        public final String webhook;

        public ImageCall(Long taskId, String checkpoint, String lora, String prompt, int width, int height, String webhook) {
            this.taskId = taskId;
            this.checkpoint = checkpoint;
            this.lora = lora;
            this.prompt = prompt;
            this.width = width;
            this.height = height;
            this.webhook = webhook;
        }
    }

    public final List<ImageCall> imageCalls = new ArrayList<>();

    @Override
    public String modifyPrompt(String lora, String prompt) {
        return "[FAKE_MODIFIED] " + prompt;
    }

    @Override
    public boolean censorPrompt(String prompt) {return true;};

    @Override
    public String createImage(Long taskId, String checkpoint, String lora, String prompt, String negativePrompt, int width, int height, String webhook) {
        imageCalls.add(new ImageCall(taskId, checkpoint, lora, prompt, width, height, webhook));  // 호출 기록
        return String.format("{\"status\":\"ok\",\"type\":\"image\",\"task_id\":%d}", taskId);
    }

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

}
