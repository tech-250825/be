package com.ll.demo03.UGC.domain;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UGC {

    private Long id;
    private String url;
    private int index;
    private ImageTask imageTask;
    private com.ll.demo03.videoTask.domain.VideoTask videoTask;
    private Long createdAt;
    private Long modifiedAt;
    private Member creator;

    @Builder
    public UGC(Long id, String url, int index, ImageTask imageTask, com.ll.demo03.videoTask.domain.VideoTask videoTask, Long createdAt, Long modifiedAt, Member creator){
        this.id = id;
        this.url = url;
        this.index = index;
        this.imageTask = imageTask;
        this.videoTask = videoTask;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
    }

    public static UGC ofImage(String url, ImageTask imageTask) {
        return UGC.builder()
                .url(url)
                .imageTask(imageTask)
                .creator(imageTask.getCreator())
                .build();
    }

    public static UGC ofVideo(String url, com.ll.demo03.videoTask.domain.VideoTask videoTask) {
        return UGC.builder()
                .url(url)
                .videoTask(videoTask)
                .creator(videoTask.getCreator())
                .build();
    }

}
