package com.ysy.myapp.project.controller;

import com.ysy.myapp.project.auth.Auth;
import com.ysy.myapp.project.auth.AuthService;
import com.ysy.myapp.project.entity.FinancialHistory;
import com.ysy.myapp.project.entity.FinancialHistoryRepository;
import com.ysy.myapp.project.entity.Member;
import com.ysy.myapp.project.entity.MemberRepository;
import com.ysy.myapp.project.request.DepositRequest;
import com.ysy.myapp.project.request.WithdrawRequest;
import com.ysy.myapp.project.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name="금융기록 관리 처리 API")
@RestController
@RequestMapping(value = "/financialHistories")
public class FinancialHistoryController {
    private List<FinancialHistory> financialHistoryList;
    private Map<FinancialHistory, Long> balanceData = new HashMap<>();
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MemberRepository authMemberRepo;
    @Autowired
    private FinancialHistoryRepository repo;
    @Autowired
    private AuthService authService;

    // 기록추가
    @Operation(summary = "금융기록 추가", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFinancialHistory(
            @RequestBody FinancialHistory financialHistory,
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
            Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }

            // 데이터베이스에 기록 추가
            financialHistory.setMember(member);
            financialHistory.setDate(LocalDate.parse(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

            FinancialHistory savedFinancialHistory = authService.createAndAddFinancialHistory(financialHistory);

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

    @Operation(summary = "금융기록추가, 입금", security = { @SecurityRequirement(name = "bearer-key") })
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
            Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }
            LocalDate finalSelectedDate = selectedDate;
            Optional<FinancialHistory> existingHistory = member.getFinancialHistories().stream()
                    .filter(history -> history.getDate().equals(finalSelectedDate))
                    .findFirst();

            FinancialHistory savedFinancialHistory;
            if (existingHistory.isPresent()) {
                // 기존 입금 기록에 추가
                FinancialHistory currentHistory = existingHistory.get();
                currentHistory.setDeposit(currentHistory.getDeposit() + request.getDeposit());
                currentHistory.setBalance(currentHistory.getBalance() + request.getDeposit());
                savedFinancialHistory = repo.save(currentHistory);
            } else {
                // 입금 기록 생성 및 저장
                FinancialHistory financialHistory = FinancialHistory.builder()
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

    @Operation(summary = "금융기록추가, 출금", security = { @SecurityRequirement(name = "bearer-key") })
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
            Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }
            LocalDate finalSelectedDate = selectedDate;
            Optional<FinancialHistory> existingHistory = member.getFinancialHistories().stream()
                    .filter(history -> history.getDate().equals(finalSelectedDate))
                    .findFirst();

            FinancialHistory savedFinancialHistory;
            if (existingHistory.isPresent()) {
                // 기존 출금 기록에 추가
                FinancialHistory currentHistory = existingHistory.get();
                currentHistory.setWithdraw(currentHistory.getWithdraw() + request.getWithdraw());
                currentHistory.setBalance(currentHistory.getBalance() - request.getWithdraw());
                savedFinancialHistory = repo.save(currentHistory);
            } else {
                // 출금 기록 생성 및 저장
                FinancialHistory financialHistory = FinancialHistory.builder()
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

    @Operation(summary = "잔액계산")
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
    @Operation(summary = "유저정보조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping
    public List<FinancialHistory> view(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member data is missing");
        }

        return repo.findAllByMemberOrderByDate(member);
    }

    @Operation(summary = "날짜정보로 유저 조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping("/by-date/{date}")
    public ResponseEntity<List<FinancialHistory>> getFinancialHistoryByDate(@PathVariable String date, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member data is missing");
        }

        List<FinancialHistory> filteredList = repo.findByDateAndMember(LocalDate.parse(date), member);
        return ResponseEntity.ok(filteredList);
    }


    @Operation(summary = "월단위 유저 데이터 조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping("/by-month/{year}-{month}")
    public ResponseEntity<List<FinancialHistory>> getBalanceByMonth(@PathVariable int year, @PathVariable int month, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member data is missing");
        }

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<FinancialHistory> financialHistories = repo.findByDateBetweenAndMember(startOfMonth, endOfMonth, member);
        return ResponseEntity.ok(financialHistories);
    }

    @Operation(summary = "아이디로 유저이름 조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping("/getName")
    public ResponseEntity<Map<String, Object>> getUserName(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> res = new HashMap<>();

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                res.put("data", null);
                res.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            Member member = authMemberRepo.findById(String.valueOf(Long.parseLong(userId))).orElse(null);

            if (member == null) {
                res.put("data", null);
                res.put("message", "Member data is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
            }

            String name = member.getName();
            res.put("name", name);
            res.put("message", "success");
            return ResponseEntity.status(HttpStatus.OK).body(res);

        } catch (Exception e) {
            res.put("data", null);
            res.put("message", "Failed to fetch the username");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // 기록 수정
    @Operation(summary = "금융기록 수정", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @PutMapping("/update/{id}/{dateStr}")
    public ResponseEntity<Map<String, Object>> updateFinancialHistory(@PathVariable("id") long id,
                                                                      @PathVariable String dateStr,
                                                                      @RequestBody FinancialHistory updatedHistory,
                                                                      @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);


        System.out.println(dateStr);
        System.out.println("Date: " + dateStr);
        System.out.println("ID: " + id);
        if (userId == null) {
            return unauthorizedResponse("Invalid token");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return badRequestResponse("잘못된 날짜 형식입니다.");
        }

        Optional<FinancialHistory> existingHistory = repo.findByDateAndId(date,id);
        if (existingHistory.isEmpty()) {
            return badRequestResponse("금융 기록을 찾을 수 없습니다.");
        }

        FinancialHistory currentHistory = existingHistory.get();
        currentHistory.setDate(updatedHistory.getDate());
        currentHistory.setDeposit(updatedHistory.getDeposit());
        currentHistory.setWithdraw(updatedHistory.getWithdraw());
        currentHistory.setBalance(updatedHistory.getBalance());
//        currentHistory.setId(updatedHistory.getId());

        FinancialHistory savedFinancialHistory = repo.save(currentHistory);

        Map<String, Object> res = new HashMap<>();
        res.put("data", savedFinancialHistory);
        res.put("message", "수정완료");
        return ResponseEntity.ok(res);
    }

    // 날짜로 기록 삭제
    @Operation(summary = "금융기록 삭제", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @Transactional
    @DeleteMapping("/delete/{date}")
    public ResponseEntity<Map<String, Object>> deleteFinancialHistory(@PathVariable String date,
                                                                      @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            return unauthorizedResponse("Invalid token");
        }
        Optional<Member> optionalAuthMember = authMemberRepo.findById(String.valueOf(Long.parseLong(userId)));
        if (!optionalAuthMember.isPresent()) {
            return badRequestResponse("유저를 찾을 수 없습니다.");
        }
        Long memberId = optionalAuthMember.get().getId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        int deletedCount = repo.deleteByDateAndId(localDate, memberId);

        if (deletedCount > 0) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "삭제 완료");
            return ResponseEntity.ok(res);
        } else {
            return badRequestResponse("금융 기록을 찾을 수 없습니다.");
        }
    }

    // 공통응답
    private ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    private ResponseEntity<Map<String, Object>> badRequestResponse(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
