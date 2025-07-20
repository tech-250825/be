package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "video_tasks")
public class VideoTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prompt;

    private String lora;

    private String runpodId;

    private Status status;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

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