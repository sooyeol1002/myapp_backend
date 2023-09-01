package com.ysy.myapp.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "AuthMember")
public class Member {
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
    @Column(nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<FinancialHistory> financialHistories = new ArrayList<>();

    public void addFinancialHistory(FinancialHistory financialHistory) {
        financialHistories.add(financialHistory);
        financialHistory.setMember(this);
    }

}