package com.ll.demo03.imageTask.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.port.ResponseConverter;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.global.domain.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImageTaskResponseConverter implements ResponseConverter<ImageTask, TaskOrImageResponse> {
    private final UGCRepository ugcRepository;

    public ImageTaskResponseConverter(UGCRepository ugcRepository) {
        this.ugcRepository = ugcRepository;
    }

    @Override
    public List<TaskOrImageResponse> convertToResponses(List<ImageTask> tasks) {
        List<ImageTask> completedTasks = tasks.stream()
                .filter(task -> Status.COMPLETED.equals(task.getStatus()))
                .collect(Collectors.toList());

        Map<Long, List<UGC>> taskImageMap = Collections.emptyMap();
        if (!completedTasks.isEmpty()) {
            taskImageMap = ugcRepository.findByImageTaskIn(completedTasks)
                    .stream()
                    //                .collect(Collectors.groupingBy(UGC::getImageTask)); UGC::getImageTask()가 equals()/hashCode() 구현이 없어서 Map 키 매핑에 실패 ..DB에서 조회된 imageTask는 영속 개체인데 tasks 리스트의 ImageTask는 다른 객체로 메모리상 같지 않음
                    .collect(Collectors.groupingBy(ugc -> ugc.getImageTask().getId()));
        }

        List<TaskOrImageResponse> responses = new ArrayList<>();

        for (ImageTask task : tasks) {
            if (Status.IN_PROGRESS.equals(task.getStatus())|| Status.FAILED.equals(task.getStatus())) {
                responses.add(TaskOrImageResponse.fromTask(task));
            } else if (Status.COMPLETED.equals(task.getStatus())) {
                List<UGC> images = taskImageMap.getOrDefault(task.getId(), Collections.emptyList());
                for (UGC image : images) {
                    responses.add(TaskOrImageResponse.fromImage(task, image));
                }
            }
        }

        return responses;
    }
}