package com.ysy.myapp.auth.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Component
public class WithdrawRequest {
    private LocalDate selectedDate;
    private long withdraw;
    private long balance;
}
