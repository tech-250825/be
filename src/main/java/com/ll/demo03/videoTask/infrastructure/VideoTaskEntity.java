package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class VideoTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prompt;

    private String lora;

    private String runpodId;

    private String status;

    @Column(name= "created_at")
    private Long createdAt;

    @Column(name = "modified_at")
    private Long modifiedAt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity creator;

    public static VideoTaskEntity from(VideoTask videoTask) {
        VideoTaskEntity videoTaskEntity = new VideoTaskEntity();
        videoTaskEntity.id = videoTask.getId();
        videoTaskEntity.prompt = videoTask.getPrompt();
        videoTaskEntity.lora = videoTask.getLora();
        videoTaskEntity.runpodId = videoTask.getRunpodId();
        videoTaskEntity.status = videoTask.getStatus();
        videoTaskEntity.createdAt = videoTask.getCreatedAt();
        videoTaskEntity.modifiedAt = videoTask.getModifiedAt();
        videoTaskEntity.creator = MemberEntity.from(videoTask.getCreator());

        return videoTaskEntity;
    }

    public VideoTask toModel() {
        return VideoTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator.toModel())
                .build();
    }
}