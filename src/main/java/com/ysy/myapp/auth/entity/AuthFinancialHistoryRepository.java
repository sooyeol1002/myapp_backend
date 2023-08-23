package com.ysy.myapp.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthFinancialHistoryRepository extends JpaRepository<AuthFinancialHistory, String> {
    Optional<AuthFinancialHistory> findByMember_Id(Long id);
    List<AuthFinancialHistory> findAllByOrderByDate();

    List<AuthFinancialHistory> findByDate(String date);
}
