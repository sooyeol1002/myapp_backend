package com.ysy.myapp.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthFinancialHistoryRepository extends JpaRepository<AuthFinancialHistory, Long> {
    Optional<AuthFinancialHistory> findByMember_Id(Long id);
    List<AuthFinancialHistory> findAllByOrderByDate();

    List<AuthFinancialHistory> findAllByMemberOrderByDate(AuthMember member);

    List<AuthFinancialHistory> findByDateAndMember(LocalDate parse, AuthMember member);

    List<AuthFinancialHistory> findByDateBetweenAndMember(LocalDate startOfMonth, LocalDate endOfMonth, AuthMember member);
}
