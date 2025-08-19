package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.global.domain.ResolutionProfile;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.weight.infrasturcture.WeightEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "image_tasks")
public class ImageTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String oldPrompt;

    @ManyToOne
    @JoinColumn(name = "checkpoint_id", nullable = true)
    private WeightEntity checkpoint;

    @ManyToOne
    @JoinColumn(name = "lora_id", nullable = true)
    private WeightEntity lora;

    private String runpodId;

    @Enumerated(EnumType.STRING)
    private ResolutionProfile resolutionProfile;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    public static ImageTaskEntity from(ImageTask task) {
        ImageTaskEntity taskEntity = new ImageTaskEntity();
        taskEntity.id = task.getId();
        taskEntity.checkpoint = task.getCheckpoint() != null ? WeightEntity.from(task.getCheckpoint()) : null;
        taskEntity.lora = task.getLora() != null ? WeightEntity.from(task.getLora()) : null;
        taskEntity.oldPrompt = task.getOldPrompt();
        taskEntity.prompt = task.getPrompt();
        taskEntity.runpodId = task.getRunpodId();
        taskEntity.resolutionProfile = task.getResolutionProfile();
        taskEntity.status = task.getStatus();
        taskEntity.createdAt = task.getCreatedAt();
        taskEntity.modifiedAt = task.getModifiedAt();
        taskEntity.member = MemberEntity.from(task.getCreator());

        return taskEntity;
    }

    public ImageTask toModel() {
        return ImageTask.builder()
                .id(id)
                .prompt(prompt)
                .oldPrompt(oldPrompt)
                .checkpoint(checkpoint != null ? checkpoint.toModel():null )
                .lora(lora != null ? lora.toModel() : null)
                .runpodId(runpodId)
                .resolutionProfile(resolutionProfile)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(member.toModel())
                .build();
    }
}