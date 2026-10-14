package com.example.cgi.repository;

import com.example.cgi.entity.AccountBalance;
import com.example.cgi.model.Currency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    List<AccountBalance> findAllByAccount_Id(Long accountId);

//    Optional<AccountBalance> findByAccount_IdAndCurrency(
//            Long accountId,
//            Currency currency
//    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b from AccountBalance b
        where b.account.id = :accountId
          and b.currency = :currency
        """)
    Optional<AccountBalance> findForUpdate(
            @Param("accountId") Long accountId,
            @Param("currency") Currency currency
    );
}