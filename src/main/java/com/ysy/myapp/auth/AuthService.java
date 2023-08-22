package com.ysy.myapp.auth;

import com.ysy.myapp.auth.entity.AuthFinancialHistory;
import com.ysy.myapp.auth.entity.AuthMember;
import com.ysy.myapp.auth.entity.AuthFinancialHistoryRepository;
import com.ysy.myapp.auth.entity.AuthMemberRepository;
import com.ysy.myapp.auth.request.SignupRequest;
import com.ysy.myapp.auth.util.HashUtil;
import com.ysy.myapp.member.Member;
import com.ysy.myapp.member.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberRepository memberRepo;
    private AuthMemberRepository repo;
    private AuthFinancialHistoryRepository finanHistoryRepo;

    @Autowired
    private HashUtil hash;

    @Autowired
    public AuthService(MemberRepository memberRepo, AuthMemberRepository repo, AuthFinancialHistoryRepository finanHistoryRepo) {
        this.memberRepo = memberRepo;
        this.repo = repo;
        this.finanHistoryRepo = finanHistoryRepo;
    }
    @Transactional
    public long createIdentity(SignupRequest req) {
        // 1. login 정보를 insert
        AuthMember toSaveLogin =
                AuthMember.builder()
                        .name(req.getName())
                        .secret(hash.createHash(req.getPassword()))
                        .build();
        AuthMember savedLogin = repo.save(toSaveLogin);

        // 2. profile 정보를 insert(login_id포함)하고 레코드의 id값을 가져옴;
        AuthFinancialHistory toSaveFinancialHistory =
                AuthFinancialHistory.builder()
                        .date(req.getDate())
                        .deposit(req.getDeposit())
                        .withdraw(req.getWithdraw())
                        .balance(req.getBalance())
                        .member(savedLogin)
                        .build();
        long financialHistoryId = finanHistoryRepo.save(toSaveFinancialHistory).getId();

        // 3. 로그인 정보에는 profile_id값만 저장
        savedLogin.setFinancialHistoryId(financialHistoryId);
        repo.save(savedLogin);

        // 4. profile_id를 반환
        return financialHistoryId;
    }
    @Transactional
    public void signUpAndSave(SignupRequest req) {
        Member member = new Member(req.getName(), req.getPassword(), req.getPhone(), req.getEmail());
        memberRepo.save(member);
        AuthMember authMember = new AuthMember(req.getName(), req.getPassword(), req.getPhone(), req.getEmail());
        repo.save(authMember);
    }
}
