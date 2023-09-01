package com.ysy.myapp.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthFinancialHistoryRepository extends JpaRepository<AuthFinancialHistory, Long> {
    List<AuthFinancialHistory> findByMember_Id(Long id);
    List<AuthFinancialHistory> findAllByOrderByDate();

    List<AuthFinancialHistory> findAllByMemberOrderByDate(AuthMember member);

    List<AuthFinancialHistory> findByDateAndMember(LocalDate parse, AuthMember member);

    List<AuthFinancialHistory> findByDateBetweenAndMember(LocalDate startOfMonth, LocalDate endOfMonth, AuthMember member);

    Optional<AuthFinancialHistory> findByDate(LocalDate date);

    @Modifying
    @Query("delete from AuthFinancialHistory f where f.date = :date and f.member.id = :id")
    int deleteByDateAndId(LocalDate date, Long id);


}
