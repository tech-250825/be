package com.ll.demo03.board.infrastructure;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "board")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public static BoardEntity from(Board board){
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.id = board.getId();
        boardEntity.name = board.getName();
        boardEntity.member = MemberEntity.from(board.getMember());
        boardEntity.createdAt = board.getCreatedAt();
        boardEntity.modifiedAt = board.getModifiedAt();

        return boardEntity;
    }

    public Board toModel() {
        return Board.builder()
                .id(id)
                .name(name)
                .member(member.toModel())
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }
}
