package com.ll.demo03.global.port;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;

public interface MessageProducer {

    void sendImageCreationMessage(ImageQueueRequest message);

    void sendCreationMessage(T2VQueueRequest message);

    void sendCreationMessage(I2VQueueRequest message);

    void sendLastFrameMessage(I2VQueueRequest message);
}
