package com.ll.demo03.imageTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequestV2;
import com.ll.demo03.imageTask.controller.response.TaskOrI2IResponse;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.member.domain.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface I2ITaskService {
    void initate(I2ITaskRequest request, Member member);
    void initate(I2ITaskRequestV2 request, Member member, MultipartFile image);
    void processCreationTransactional(I2IQueueRequest message);
    PageResponse<List<TaskOrI2IResponse>>  getMyTasks(Member member, CursorBasedPageable pageable);
}