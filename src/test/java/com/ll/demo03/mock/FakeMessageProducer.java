package com.ll.demo03.mock;

import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueV3Request;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;

import java.util.ArrayList;
import java.util.List;

public class FakeMessageProducer implements MessageProducer {

    public List<ImageQueueRequest> imageMessages = new ArrayList<>();
    public List<ImageQueueV3Request> plainMessages = new ArrayList<>();
    public List<T2VQueueRequest> t2vMessages = new ArrayList<>();
    public List<I2VQueueRequest> i2vMessages = new ArrayList<>();
    public List<I2IQueueRequest> i2iMessages = new ArrayList<>();

    @Override
    public void sendImageCreationMessage(ImageQueueRequest message) {
        imageMessages.add(message);
    }

    @Override
    public void sendFaceDetailerCreationMessage(ImageQueueRequest message) {imageMessages.add(message);}

    @Override
    public void sendPlainCreationMessage(ImageQueueV3Request message) {plainMessages.add(message);}

    @Override
    public void sendCreationMessage(T2VQueueRequest message) {
        t2vMessages.add(message);
    }

    @Override
    public void sendCreationMessage(I2VQueueRequest message) {
        i2vMessages.add(message);
    }
    @Override
    public void sendCreationMessage(I2IQueueRequest message) {
        i2iMessages.add(message);
    }
}

