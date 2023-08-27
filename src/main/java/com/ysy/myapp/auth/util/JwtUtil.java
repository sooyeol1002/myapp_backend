package com.ysy.myapp.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ysy.myapp.auth.entity.AuthMember;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class JwtUtil {

    // 임의의 서명 값 - 키파일 등을 읽어서 처리가능
    public String secret = "secret";
    // ms 단위
    public final long TOKEN_TIMEOUT = 1000 * 60 * 60 * 24 * 7;


    // JWT 토큰 생성
    public String createToken(long id, String name) {
        // 토큰 생성시간과 만료시간을 만듦
        Date now = new Date();
        // 만료시간: 2차인증 이런게 잘걸려있으면 큰문제는 안됨. 내컴퓨터를 다른 사람이 쓴다.
        // 길게: 7일~30일
        // 보통: 1시간~3시간
        // 짧게: 5분~15분
        Date exp = new Date(now.getTime() + TOKEN_TIMEOUT);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                // sub: 토큰 소유자
                .withSubject(String.valueOf(id))
                .withClaim("name", name)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    public AuthMember validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 검증 객체 생성
        JWTVerifier verifier = JWT.require(algorithm).build();
        String modifyToken = token.trim();

        try {
            DecodedJWT decodedJWT = verifier.verify(modifyToken);
            // 토큰 검증 제대로 된 상황
            // 토큰 페이로드(데이터, subject/claim)를 조회
            Long id = Long.valueOf(decodedJWT.getSubject());
            String name = decodedJWT
                    .getClaim("name").asString();
            return AuthMember.builder()
                    .id(id)
                    .name(name)
                    .build();

        } catch (JWTVerificationException e) {
            System.out.println("token verification failed: " + e.getMessage());
            // 토큰 검증 오류 상황
            return null;
        }
    }

    public String extractUserId(String token){
        AuthMember authMember = validateToken(token);
        if (authMember != null) {
            return String.valueOf(authMember.getId());
        }
        return "unknown";
    }
}
