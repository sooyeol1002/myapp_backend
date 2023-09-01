package com.ysy.myapp.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "AuthFinancialHistory")
public class FinancialHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = true)
    private LocalDate date;
    @Column(nullable = false)
    private long deposit;
    @Column(nullable = false)
    private long withdraw;
    @Column(nullable = false)
    private long balance;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
    @Override
    public String toString() {
        return "AuthFinancialHistory{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", deposit=" + deposit +
                ", withdraw=" + withdraw +
                ", balance=" + balance +
                '}';
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
