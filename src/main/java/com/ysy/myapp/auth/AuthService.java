package com.ysy.myapp.auth;

import com.ysy.myapp.auth.entity.AuthFinancialHistory;
import com.ysy.myapp.auth.entity.AuthMember;
import com.ysy.myapp.auth.entity.AuthFinancialHistoryRepository;
import com.ysy.myapp.auth.entity.AuthMemberRepository;
import com.ysy.myapp.auth.request.SignupRequest;
import com.ysy.myapp.auth.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    @Autowired
    private AuthMemberRepository repo;
    @Autowired
    private AuthFinancialHistoryRepository finanHistoryRepo;

    @Autowired
    private HashUtil hash;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    public AuthService(AuthMemberRepository repo, AuthFinancialHistoryRepository finanHistoryRepo) {
        this.repo = repo;
        this.finanHistoryRepo = finanHistoryRepo;
    }
    @Transactional
    public AuthMember createIdentity(SignupRequest req) {
        // 1. login 정보를 insert
        AuthMember toSaveLogin =
                AuthMember.builder()
                        .name(req.getName())
                        .secret(hash.createHash(req.getPassword()))
                        .deposit(0)
                        .withdraw(0)
                        .balance(0)
                        .date(LocalDate.now()) // 현재 날짜를 기본값으로 설정
                        .build();

        // 2. profile 정보를 insert(login_id포함)하고 레코드의 id값을 가져옴;
        AuthFinancialHistory toSaveFinancialHistory =
                AuthFinancialHistory.builder()
                        .date(toSaveLogin.getDate())
                        .deposit(req.getDeposit())
                        .withdraw(req.getWithdraw())
                        .balance(req.getBalance())
                        .member(toSaveLogin)
                        .build();

        // 3. 로그인 정보에는 profile_id값만 저장
        toSaveLogin.addFinancialHistory(toSaveFinancialHistory);

        AuthMember savedMember = repo.save(toSaveLogin);
        createAndAddFinancialHistory(toSaveFinancialHistory);

        // 4. profile_id를 반환
        return savedMember;
    }

    @Transactional
    public AuthFinancialHistory createAndAddFinancialHistory(AuthFinancialHistory financialHistory) {
        if (financialHistory == null) {
            throw new IllegalArgumentException("Financial history data is missing");
        }

        AuthMember member = financialHistory.getMember();
        if (member == null) {
            throw new IllegalArgumentException("Member data is missing");
        }

        AuthFinancialHistory savedFinancialHistory = finanHistoryRepo.save(financialHistory);
        System.out.println("Financial history saved: " + savedFinancialHistory);

        if (member.getFinancialHistories() == null) {
            member.setFinancialHistories(new ArrayList<>());
        }
        member.getFinancialHistories().add(savedFinancialHistory);

        return savedFinancialHistory;
    }

//    @Transactional
//    public void signUpAndSave(SignupRequest req) {
//        AuthMember authMember = new AuthMember(req.getName(), req.getPassword(), req.getPhone(), req.getEmail());
//        repo.save(authMember);
//    }
}
