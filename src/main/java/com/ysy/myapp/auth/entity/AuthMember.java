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
@Entity(name = "AuthMember")
public class AuthMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    @Column(length = 500)
    private String secret;
    @Column(nullable = false)
    private long deposit;
    @Column(nullable = false)
    private long withdraw;
    @Column(nullable = false)
    private long balance;

    @JoinColumn(name = "financial_history_id") // 외래 키 컬럼 이름
    private long financialHistoryId;

    public AuthMember(String name, String password, long phone, String email) {
    }
}