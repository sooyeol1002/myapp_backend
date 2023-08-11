package com.ysy.myapp.backend.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository <Member, String> {
    @Query(value = "select * " +
            "from member " +
            "where name = name", nativeQuery = true)
    Optional<Member> findMemberByName(String name);

    List<Member> findAllByOrderByName();

    Optional<Member> findByName(String name);
}
