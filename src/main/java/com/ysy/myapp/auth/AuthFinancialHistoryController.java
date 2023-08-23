package com.ysy.myapp.auth;

import com.ysy.myapp.auth.entity.AuthFinancialHistory;
import com.ysy.myapp.auth.entity.AuthFinancialHistoryRepository;
import com.ysy.myapp.auth.entity.AuthMember;
import com.ysy.myapp.auth.entity.AuthMemberRepository;
import com.ysy.myapp.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/financialHistories")
public class AuthFinancialHistoryController {
    private List<AuthFinancialHistory> financialHistoryList;
    private Map<AuthFinancialHistory, Long> balanceData = new HashMap<>();
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthMemberRepository authMemberRepo;

    @Autowired
    AuthFinancialHistoryRepository repo;

    @GetMapping
    public List<AuthFinancialHistory> view(){
        List<AuthFinancialHistory> list = repo.findAllByOrderByDate();
        return list;
    }

    // 기록추가
    @Auth
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFinancialHistory(
            @RequestBody AuthFinancialHistory financialHistory) {

        if (financialHistory.getDate() == null || financialHistory.getDate().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "[date] field is required");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(res);
        }

        AuthFinancialHistory savedFinancialHistory = repo.save(financialHistory);
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
    public ResponseEntity<List<AuthFinancialHistory>> getFinancialHistoryByDate(@PathVariable String date) {
        List<AuthFinancialHistory> filteredList = repo.findByDate(date);
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
    public ResponseEntity<List<AuthFinancialHistory>> getBalanceByMonth(@PathVariable String month) {
        // System.out.println("Requested month: " + month);
        List<AuthFinancialHistory> financialHistories = repo.findByDate(month);
        return ResponseEntity.ok(financialHistories);
    }
}
