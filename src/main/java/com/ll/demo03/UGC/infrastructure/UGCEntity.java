package com.ll.demo03.UGC.infrastructure;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name="ugc")
public class UGCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Column(name = "ugc_index")
    private int index;

    @Nullable
    @ManyToOne(fetch= FetchType.LAZY)
    private ImageTaskEntity imageTask;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private VideoTaskEntity videoTask;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity member;

    public static UGCEntity from(UGC ugc) {
        UGCEntity ugcEntity = new UGCEntity();
        ugcEntity.id = ugc.getId();
        ugcEntity.url = ugc.getUrl();
        ugcEntity.index = ugc.getIndex();
        ugcEntity.imageTask = ImageTaskEntity.from(ugc.getImageTask());
        ugcEntity.videoTask = VideoTaskEntity.from(ugc.getVideoTask());
        ugcEntity.member = MemberEntity.from(ugc.getCreator());

        return ugcEntity;
    }

    public UGC toModel() {
        return UGC.builder()
                .id(id)
                .url(url)
                .index(index)
                .imageTask(imageTask.toModel())
                .videoTask(videoTask.toModel())
                .createdAt(createdAt)
                .creator(member.toModel())
                .build();
    }
}