package com.ll.demo03.imageTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTaskInitiate;
import com.ll.demo03.member.domain.Member;

import java.util.List;

public interface ImageTaskService {
    void initate(ImageTaskRequest request, Member member);
    void processImageCreationTransactional(ImageTaskInitiate message);
    PageResponse<List<TaskOrImageResponse>> getMyTasks(Member member, CursorBasedPageable pageable);
}
