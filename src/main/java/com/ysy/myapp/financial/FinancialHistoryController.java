package com.ysy.myapp.financial;

import com.ysy.myapp.member.Member;
import com.ysy.myapp.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/financialHistories")
public class FinancialHistoryController {
    private List<FinancialHistory> financialHistoryList;
    private Map<FinancialHistory, Long> balanceData = new HashMap<>();

    @Autowired
    FinancialHistoryRepository repo;

    @GetMapping
    public List<FinancialHistory> view(){
        List<FinancialHistory> list = repo.findAllByOrderByDate();
        return list;
    }

    // 기록추가
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFinancialHistory(
            @RequestBody FinancialHistory financialHistory) {
        if (financialHistory.getDate() == null || financialHistory.getDate().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "[date] field is required");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(res);
        }
        FinancialHistory savedFinancialHistory = repo.save(financialHistory);
        if (savedFinancialHistory != null) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", savedFinancialHistory);
            res.put("message", "created");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        return ResponseEntity.ok().build();
    }

    // 날짜값으로 데이터를 조회
    @GetMapping("/by-date/{date}")
    public ResponseEntity<List<FinancialHistory>> getFinancialHistoryByDate(@PathVariable String date) {
        List<FinancialHistory> filteredList = repo.findByDate(date);
        return ResponseEntity.ok(filteredList);
    }

    // 잔액계산
    @PostMapping("calculate-balance")
    public ResponseEntity<Map<String, Long>> calculateBalance(@RequestBody Map<String, Long> data) {
        Long deposit = data.get("deposit");
        Long withdraw = data.get("withdraw");
        if (deposit == null || withdraw == null) {
            Map<String, Long> res = new HashMap<>();
            res.put("balance", 0L);

            return ResponseEntity.badRequest().body(res);
        }

        Long balance = deposit - withdraw;
        Map<String, Long> res = new HashMap<>();
        res.put("balance", balance);

        return ResponseEntity.ok(res);
    }

    // 월별 DB 저장값 조회
    // 2023-05 이런형식으로 조회해야 조회가 됨.
    @GetMapping("/by-month/{month}")
    public ResponseEntity<List<FinancialHistory>> getBalanceByMonth(@PathVariable String month) {
        // System.out.println("Requested month: " + month);
        List<FinancialHistory> financialHistories = repo.findByDate(month);
        return ResponseEntity.ok(financialHistories);
    }
}
