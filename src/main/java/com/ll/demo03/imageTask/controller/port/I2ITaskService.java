package com.ll.demo03.imageTask.controller.port;

import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequestV2;
import com.ll.demo03.member.domain.Member;
import org.springframework.web.multipart.MultipartFile;

public interface I2ITaskService {
    void initate(I2ITaskRequest request, Member member);
    void initate(I2ITaskRequestV2 request, Member member, MultipartFile image);
    void processCreationTransactional(I2IQueueRequest message);
}