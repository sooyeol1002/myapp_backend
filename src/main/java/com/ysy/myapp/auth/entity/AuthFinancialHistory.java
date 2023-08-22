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
    private String date;
    @Column(nullable = false)
    private long deposit;
    @Column(nullable = false)
    private long withdraw;
    @Column(nullable = false)
    private long balance;

    @OneToOne
    private AuthMember member;

    public long getId() {
        return 0;
    }
}
