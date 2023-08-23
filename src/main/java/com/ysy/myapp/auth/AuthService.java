package com.ysy.myapp.auth;

import com.ysy.myapp.auth.entity.AuthFinancialHistory;
import com.ysy.myapp.auth.entity.AuthMember;
import com.ysy.myapp.auth.entity.AuthFinancialHistoryRepository;
import com.ysy.myapp.auth.entity.AuthMemberRepository;
import com.ysy.myapp.auth.request.SignupRequest;
import com.ysy.myapp.auth.util.HashUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private AuthMemberRepository repo;
    private AuthFinancialHistoryRepository finanHistoryRepo;

    @Autowired
    private HashUtil hash;
    private long financialHistoryId;

    @Autowired
    public AuthService( AuthMemberRepository repo, AuthFinancialHistoryRepository finanHistoryRepo) {
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
        long memberId = finanHistoryRepo.save(toSaveFinancialHistory).getMemberId();

        // 3. 로그인 정보에는 profile_id값만 저장
        savedLogin.setFinancialHistoryId(financialHistoryId);
        repo.save(savedLogin);

        // 4. profile_id를 반환
        return memberId;
    }
//    @Transactional
//    public void signUpAndSave(SignupRequest req) {
//        AuthMember authMember = new AuthMember(req.getName(), req.getPassword(), req.getPhone(), req.getEmail());
//        repo.save(authMember);
//    }
}
