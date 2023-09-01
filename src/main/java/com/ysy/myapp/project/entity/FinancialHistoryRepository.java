package com.ysy.myapp.project.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialHistoryRepository extends JpaRepository<FinancialHistory, Long> {
    List<FinancialHistory> findByMember_Id(Long id);
    List<FinancialHistory> findAllByOrderByDate();

    List<FinancialHistory> findAllByMemberOrderByDate(Member member);

    List<FinancialHistory> findByDateAndMember(LocalDate parse, Member member);

    List<FinancialHistory> findByDateBetweenAndMember(LocalDate startOfMonth, LocalDate endOfMonth, Member member);

    Optional<FinancialHistory> findByDate(LocalDate date);

    Optional<FinancialHistory> findByMember_DateAndId(LocalDate date, long id);


    @Modifying
    @Query("delete from AuthFinancialHistory f where f.date = :date and f.member.id = :id")
    int deleteByDateAndId(LocalDate date, Long id);


}
