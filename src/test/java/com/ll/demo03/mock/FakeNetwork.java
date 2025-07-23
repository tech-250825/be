package com.ll.demo03.mock;

import com.ll.demo03.global.port.Network;

import java.util.ArrayList;
import java.util.List;

public class FakeNetwork implements Network {

    public static class ImageCall {
        public final Long taskId;
        public final String lora;
        public final String prompt;
        public final String webhook;

        public ImageCall(Long taskId, String lora, String prompt, String webhook) {
            this.taskId = taskId;
            this.lora = lora;
            this.prompt = prompt;
            this.webhook = webhook;
        }
    }

    public final List<ImageCall> imageCalls = new ArrayList<>();

    @Override
    public String modifyPrompt(String lora, String prompt) {
        return "[FAKE_MODIFIED] " + prompt;
    }

    @Override
    public String createImage(Long taskId, String lora, String prompt, String webhook) {
        imageCalls.add(new ImageCall(taskId, lora, prompt, webhook));  // 호출 기록
        return String.format("{\"status\":\"ok\",\"type\":\"image\",\"task_id\":%d}", taskId);
    }

    @Override
    public String createVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames, String webhook) {
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }

    @Override
    public String createVideo(Long taskId, String lora, String prompt, String url, int width, int height, int numFrames , String webhook) {
        return String.format("{\"status\":\"ok\",\"type\":\"video\",\"task_id\":%d}", taskId);
    }
}
