package com.ysy.myapp.auth.request;

import com.ysy.myapp.auth.entity.AuthMember;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String name;
    private String password;
    private long phone;
    private String email;
    private String date;
    private long deposit;
    private long withdraw;
    private long balance;

    public LocalDate getParsedDate() {
        if (date == null || date.isEmpty()) {
            return null;
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    public long getDeposit() {
        return deposit;
    }

    public long getWithdraw() {
        return withdraw;
    }

    public long getBalance() {
        return balance;
    }

}
