package com.ysy.myapp.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthMemberRepository extends JpaRepository<AuthMember, String> {
    Optional<AuthMember> findByName(String name);
}
