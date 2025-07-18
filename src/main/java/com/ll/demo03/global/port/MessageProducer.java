package com.ll.demo03.global.port;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;

public interface MessageProducer {

    void sendImageCreationMessage(ImageQueueRequest message);

    void sendVideoCreationMessage(com.ll.demo03.videoTask.controller.request.VideoQueueRequest message);
}
