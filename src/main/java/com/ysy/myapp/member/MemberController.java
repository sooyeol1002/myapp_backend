package com.ysy.myapp.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/members")
public class MemberController {
    @Autowired
    MemberRepository repo;

    // 회원가입
    @PostMapping
    public ResponseEntity<Map<String, Object>> addMember(@RequestBody Member member) {
        if(member.getName() == null || member.getName().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "[name] field is required");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(res);
        }
        if (member.getName()!= null && repo.findByName(member.getName()).isPresent()) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("message", "동일한 정보가 이미 존재합니다.");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }
        Member savedMember = repo.save(member);
        if (savedMember != null) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", savedMember);
            res.put("message", "created");

            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @DeleteMapping(value = "/{name}")
    public ResponseEntity deleteMember(@PathVariable String name){
        if (!repo.findByName(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        repo.deleteById(name);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 조회
    @GetMapping
    public List<Member> findMemberList() {
        List<Member> list = repo.findAllByOrderByName();
        return list;
    }

    // 패스워드 검증
//    public void validatePassword() {
//
//    } 해야함
}
