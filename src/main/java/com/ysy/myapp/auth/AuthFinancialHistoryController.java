package com.ysy.myapp.auth;

import com.ysy.myapp.auth.entity.AuthFinancialHistory;
import com.ysy.myapp.auth.entity.AuthFinancialHistoryRepository;
import com.ysy.myapp.auth.entity.AuthMember;
import com.ysy.myapp.auth.entity.AuthMemberRepository;
import com.ysy.myapp.auth.request.DepositRequest;
import com.ysy.myapp.auth.request.WithdrawRequest;
import com.ysy.myapp.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private AuthFinancialHistoryRepository repo;
    @Autowired
    private AuthService authService;
    @Autowired
    private DepositRequest depositRequest;
    @GetMapping
    public List<AuthFinancialHistory> view(){
        List<AuthFinancialHistory> list = repo.findAllByOrderByDate();
        return list;
    }

    // 기록추가
    @Auth
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFinancialHistory(
            @RequestBody AuthFinancialHistory financialHistory,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // 클라이언트에서 전송한 date 값을 올바르게 파싱하여 LocalDate로 변환
            LocalDate date = financialHistory.getDate();
            if (date == null) {
                date = LocalDate.now();
            }

            // 로그로 받은 데이터 값 확인
            System.out.println("Received data from client - Date: " + financialHistory.getDate());
            System.out.println("Received data from client - Deposit: " + financialHistory.getDeposit());
            System.out.println("Received data from client - Balance: " + financialHistory.getBalance());

            // JWT 토큰에서 사용자 ID 추출
            String token = authorizationHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            // 사용자 ID로 회원 정보 가져오기
            AuthMember member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }

            // 데이터베이스에 기록 추가
            financialHistory.setMember(member);
            financialHistory.setDate(LocalDate.parse(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

            AuthFinancialHistory savedFinancialHistory = authService.createAndAddFinancialHistory(financialHistory);

            // 응답 데이터 생성
            Map<String, Object> res = new HashMap<>();
            res.put("data", savedFinancialHistory);
            res.put("message", "created");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);

        } catch (DateTimeParseException e) {
            // 날짜 변환 예외 처리
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "Invalid date format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        } catch (Exception e) {
            // 기타 예외 처리
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "Failed to create financial history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
    @Auth
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @RequestBody DepositRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = authorizationHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            LocalDate selectedDate = request.getSelectedDate() != null ? request.getSelectedDate() : LocalDate.now();
            System.out.println("Received date: " + request.getSelectedDate());
            System.out.println("Received deposit amount: " + request.getDeposit());

            if (userId == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            // 사용자 ID로 회원 정보 가져오기
            AuthMember member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }
            LocalDate finalSelectedDate = selectedDate;
            Optional<AuthFinancialHistory> existingHistory = member.getFinancialHistories().stream()
                    .filter(history -> history.getDate().equals(finalSelectedDate))
                    .findFirst();

            AuthFinancialHistory savedFinancialHistory;
            if (existingHistory.isPresent()) {
                // 기존 입금 기록에 추가
                AuthFinancialHistory currentHistory = existingHistory.get();
                currentHistory.setDeposit(currentHistory.getDeposit() + request.getDeposit());
                currentHistory.setBalance(currentHistory.getBalance() + request.getDeposit());
                savedFinancialHistory = repo.save(currentHistory);
            } else {
                // 입금 기록 생성 및 저장
                AuthFinancialHistory financialHistory = AuthFinancialHistory.builder()
                        .date(selectedDate) // 선택한 날짜 사용
                        .deposit(request.getDeposit())
                        .balance(request.getBalance())
                        .member(member)
                        .build();

                savedFinancialHistory = authService.createAndAddFinancialHistory(financialHistory);
            }

            member.setDeposit(member.getDeposit() + request.getDeposit());
            member.setBalance(member.getBalance() + request.getDeposit());
            authMemberRepo.save(member);

            Map<String, Object> res = new HashMap<>();
            res.put("data", savedFinancialHistory);
            res.put("message", "Deposit created");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);

        } catch (Exception e) {
            // 예외 처리
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "Failed to create deposit");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @Auth
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestBody WithdrawRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = authorizationHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            LocalDate selectedDate = request.getSelectedDate() != null ? request.getSelectedDate() : LocalDate.now();
            System.out.println("Received date: " + request.getSelectedDate());
            System.out.println("Received withdraw amount: " + request.getWithdraw());

            if (userId == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            // 사용자 ID로 회원 정보 가져오기
            AuthMember member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }
            LocalDate finalSelectedDate = selectedDate;
            Optional<AuthFinancialHistory> existingHistory = member.getFinancialHistories().stream()
                    .filter(history -> history.getDate().equals(finalSelectedDate))
                    .findFirst();

            AuthFinancialHistory savedFinancialHistory;
            if (existingHistory.isPresent()) {
                // 기존 출금 기록에 추가
                AuthFinancialHistory currentHistory = existingHistory.get();
                currentHistory.setWithdraw(currentHistory.getWithdraw() + request.getWithdraw());
                currentHistory.setBalance(currentHistory.getBalance() - request.getWithdraw());
                savedFinancialHistory = repo.save(currentHistory);
            } else {
                // 출금 기록 생성 및 저장
                AuthFinancialHistory financialHistory = AuthFinancialHistory.builder()
                        .date(selectedDate) // 선택한 날짜 사용
                        .withdraw(request.getWithdraw())
                        .balance(request.getBalance())
                        .member(member)
                        .build();

                savedFinancialHistory = authService.createAndAddFinancialHistory(financialHistory);
            }

            member.setWithdraw(member.getWithdraw() + request.getWithdraw());
            member.setBalance(member.getBalance() - request.getWithdraw());
            authMemberRepo.save(member);

            Map<String, Object> res = new HashMap<>();
            res.put("data", savedFinancialHistory);
            res.put("message", "Withdraw created");
            return ResponseEntity.status(HttpStatus.CREATED).body(res);

        } catch (Exception e) {
            // 예외 처리
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "Failed to create withdraw");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 날짜값으로 데이터를 조회
    @GetMapping("/by-date/{date}")
    public ResponseEntity<List<AuthFinancialHistory>> getFinancialHistoryByDate(@PathVariable String date) {
        List<AuthFinancialHistory> filteredList = repo.findByDate(LocalDate.parse(date));
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
        List<AuthFinancialHistory> financialHistories = repo.findByDate(LocalDate.parse(month));
        return ResponseEntity.ok(financialHistories);
    }
}
