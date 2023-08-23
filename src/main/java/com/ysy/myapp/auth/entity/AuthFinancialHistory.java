package com.ysy.myapp.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "AuthFinancialHistory")
public class AuthFinancialHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = true)
    private String date;
    @Column(nullable = false)
    private long deposit;
    @Column(nullable = false)
    private long withdraw;
    @Column(nullable = false)
    private long balance;

    @OneToOne
    @JoinColumn(name = "member_id")
    private AuthMember member;

    public long getId() {
        return 0;
    }

    public long getMemberId() {
        return 0;
    }
}
