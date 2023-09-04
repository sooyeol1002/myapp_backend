package com.ysy.myapp.project.controller;

import com.ysy.myapp.project.auth.AuthService;
import com.ysy.myapp.project.entity.FinancialHistory;
import com.ysy.myapp.project.entity.FinancialHistoryRepository;
import com.ysy.myapp.project.entity.Member;
import com.ysy.myapp.project.entity.MemberRepository;
import com.ysy.myapp.project.request.SignupRequest;
import com.ysy.myapp.project.util.HashUtil;
import com.ysy.myapp.project.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Tag(name="로그인 관리 API")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private MemberRepository repo;

    @Autowired
    private FinancialHistoryRepository finanHistoryRepo;

    @Autowired
    private AuthService service;

    @Autowired
    private HashUtil hash;

    @Autowired
    private JwtUtil jwt;

    @Operation(summary = "회원가입처리")
    @PostMapping(value = "/signup")
    public ResponseEntity signUp(@RequestBody SignupRequest req) {
        Member newMember = service.createIdentity(req);

        return ResponseEntity.status(HttpStatus.CREATED).body(newMember.getId());
    }

    @Operation(summary = "로그인처리")
    @PostMapping(value = "/login")
    public ResponseEntity login(
            @RequestParam String name,
            @RequestParam String password,
            HttpServletResponse res) {
        System.out.println(name);
        System.out.println(password);
        Optional<Member> login = repo.findByName(name);
        System.out.println("login: " + login);
        if(!login.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        boolean isVerified = hash.verifyHash(password, login.get().getSecret());
        if(!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Member l = login.get();
        List<FinancialHistory> finanHistory = finanHistoryRepo.findByMember_Id(l.getId());
        if(finanHistory.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String token = jwt.createToken(
                l.getId(), l.getName());

        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwt.TOKEN_TIMEOUT / 1000L)); // 만료시간
        cookie.setDomain("localhost"); // 쿠키를 사용할 수 있 도메인

        res.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500")
                        .build().toUri())
                .build();
    }
}
