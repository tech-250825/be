package com.ll.demo03.imageTask.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.port.ResponseConverter;
import com.ll.demo03.imageTask.controller.response.TaskOrI2IResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.global.domain.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class I2ITaskResponseConverter implements ResponseConverter<ImageTask, TaskOrI2IResponse> {
    private final UGCRepository ugcRepository;

    public I2ITaskResponseConverter(UGCRepository ugcRepository) {
        this.ugcRepository = ugcRepository;
    }

    @Override
    public List<TaskOrI2IResponse> convertToResponses(List<ImageTask> tasks) {
        List<ImageTask> completedTasks = tasks.stream()
                .filter(task -> Status.COMPLETED.equals(task.getStatus()))
                .collect(Collectors.toList());

        Map<Long, List<UGC>> taskImageMap = Collections.emptyMap();
        if (!completedTasks.isEmpty()) {
            taskImageMap = ugcRepository.findByImageTaskIn(completedTasks)
                    .stream()
                    .collect(Collectors.groupingBy(ugc -> ugc.getImageTask().getId()));
        }

        List<TaskOrI2IResponse> responses = new ArrayList<>();

        for (ImageTask task : tasks) {
            if (Status.IN_PROGRESS.equals(task.getStatus())|| Status.FAILED.equals(task.getStatus())) {
                responses.add(TaskOrI2IResponse.fromTask(task));
            } else if (Status.COMPLETED.equals(task.getStatus())) {
                List<UGC> images = taskImageMap.getOrDefault(task.getId(), Collections.emptyList());
                for (UGC image : images) {
                    responses.add(TaskOrI2IResponse.fromImage(task, image));
                }
            }
        }

        return responses;
    }
}