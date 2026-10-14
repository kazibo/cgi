package com.example.cgi.repository;

import com.example.cgi.entity.AccountBalance;
import com.example.cgi.model.Currency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    List<AccountBalance> findAllByAccount_Id(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountBalance> findForUpdateByAccount_IdAndCurrency(
            Long accountId,
            Currency currency
    );
}