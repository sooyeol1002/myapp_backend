package com.ysy.myapp.financial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialHistoryRepository extends JpaRepository <FinancialHistory, String> {
    @Query(value = "select * " +
            "from financial_history " +
            "where date = date", nativeQuery = true)
    Optional<FinancialHistory> findFinancialHistoryByDate(String date);

    List<FinancialHistory> findAllByOrderByDate();

    List<FinancialHistory> findByDate(String date);
}
