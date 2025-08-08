package com.ll.demo03.mock;

import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;

import java.util.ArrayList;
import java.util.List;

public class FakeMessageProducer implements MessageProducer {

    public List<ImageQueueRequest> imageMessages = new ArrayList<>();
    public List<T2VQueueRequest> t2vMessages = new ArrayList<>();
    public List<I2VQueueRequest> i2vMessages = new ArrayList<>();

    @Override
    public void sendImageCreationMessage(ImageQueueRequest message) {
        imageMessages.add(message);
    }

    @Override
    public void sendCreationMessage(T2VQueueRequest message) {
        t2vMessages.add(message);
    }

    @Override
    public void sendCreationMessage(I2VQueueRequest message) {
        i2vMessages.add(message);
    }

    @Override
    public void sendLastFrameMessage(I2VQueueRequest message) {
        i2vMessages.add(message);
    }

}

