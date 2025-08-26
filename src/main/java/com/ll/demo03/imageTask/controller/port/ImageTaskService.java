package com.ll.demo03.imageTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueV3Request;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskV3Request;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.member.domain.Member;

import java.util.List;

public interface ImageTaskService {
    void initateNsfwPlainImage(ImageTaskV3Request request, Member member);
    void initateNsfwFaceDetailer(ImageTaskRequest request, Member member);

    void processImageCreationFaceDetailer(ImageQueueRequest message);

    void processPlainCreationTransactional(ImageQueueV3Request message);
    PageResponse<List<TaskOrImageResponse>> getMyTasks(Member member, CursorBasedPageable pageable);
    void delete(Long taskId);
}
