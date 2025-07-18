package com.ll.demo03.payment.infrastructure;

import com.ll.demo03.member.infrastructure.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String orderId;

    @ManyToOne
    private Member memberEntity;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private String status;

    @Column
    private String method;

    @Column
    private LocalDateTime approvedAt;

}
