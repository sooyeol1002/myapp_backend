package com.ysy.myapp.auth.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Component
public class DepositRequest {
    private LocalDate selectedDate;
    private long deposit;
    private long balance;
}
