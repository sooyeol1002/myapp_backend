package com.ysy.myapp.auth.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String name;
    private String password;
    private long phone;
    private String email;

    public String getDate() {
        return null;
    }

    public long getDeposit() {
        return 0;
    }

    public long getWithdraw() {
        return 0;
    }

    public long getBalance() {
        return 0;
    }
}
