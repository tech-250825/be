package com.ll.demo03.board.infrastructure;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_exports")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BoardExportEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "board_id", nullable = false)
    private Long boardId;
    
    @Column(name = "video_url", length = 500, nullable = false)
    private String videoUrl;
    
    @Column(name = "status", length = 50, nullable = false)
    private String status;
    
    @Column(name = "duration")
    private Double duration;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}