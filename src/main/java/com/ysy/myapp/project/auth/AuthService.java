package com.ysy.myapp.project.auth;

import com.ysy.myapp.project.entity.FinancialHistory;
import com.ysy.myapp.project.entity.Member;
import com.ysy.myapp.project.entity.FinancialHistoryRepository;
import com.ysy.myapp.project.entity.MemberRepository;
import com.ysy.myapp.project.request.SignupRequest;
import com.ysy.myapp.project.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@Transactional
public class AuthService {
    @Autowired
    private MemberRepository repo;
    @Autowired
    private FinancialHistoryRepository finanHistoryRepo;

    @Autowired
    private HashUtil hash;

    @Transactional
    public Member createIdentity(SignupRequest req) {
        // 1. login 정보를 insert
        Member toSaveLogin =
                Member.builder()
                        .name(req.getName())
                        .secret(hash.createHash(req.getPassword()))
                        .deposit(0)
                        .withdraw(0)
                        .balance(0)
                        .date(LocalDate.now()) // 현재 날짜를 기본값으로 설정
                        .build();

        // 2. profile 정보를 insert(login_id포함)하고 레코드의 id값을 가져옴;
        FinancialHistory toSaveFinancialHistory =
                FinancialHistory.builder()
                        .date(toSaveLogin.getDate())
                        .deposit(req.getDeposit())
                        .withdraw(req.getWithdraw())
                        .balance(req.getBalance())
                        .member(toSaveLogin)
                        .build();

        // 3. 로그인 정보에는 profile_id값만 저장
        toSaveLogin.addFinancialHistory(toSaveFinancialHistory);

        Member savedMember = repo.save(toSaveLogin);
        createAndAddFinancialHistory(toSaveFinancialHistory);

        // 4. profile_id를 반환
        return savedMember;
    }

    @Transactional
    public FinancialHistory createAndAddFinancialHistory(FinancialHistory financialHistory) {
        if (financialHistory == null) {
            throw new IllegalArgumentException("Financial history data is missing");
        }

        Member member = financialHistory.getMember();
        if (member == null) {
            throw new IllegalArgumentException("Member data is missing");
        }

        FinancialHistory savedFinancialHistory = finanHistoryRepo.save(financialHistory);
        System.out.println("Financial history saved: " + savedFinancialHistory);

        if (member.getFinancialHistories() == null) {
            member.setFinancialHistories(new ArrayList<>());
        }
        member.getFinancialHistories().add(savedFinancialHistory);

        return savedFinancialHistory;
    }

}
