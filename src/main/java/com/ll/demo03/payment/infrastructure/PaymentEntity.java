package com.ll.demo03.payment.infrastructure;

import com.ll.demo03.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackId;

    @ManyToOne
    private MemberEntity member;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

}
