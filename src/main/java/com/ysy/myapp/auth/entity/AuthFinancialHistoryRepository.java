package com.ysy.myapp.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthFinancialHistoryRepository extends JpaRepository<FinancialHistory, String> {
    Optional<FinancialHistory> findByMember_Id(Long id);
}
