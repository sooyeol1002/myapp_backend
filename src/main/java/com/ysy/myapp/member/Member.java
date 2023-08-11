package com.ysy.myapp.backend.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member {
    @Id
    private String name;

    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private long phone;
    @Column(nullable = false)
    private String email;
}
