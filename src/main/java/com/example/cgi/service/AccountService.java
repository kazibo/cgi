package com.example.cgi.service;

import com.example.cgi.dto.DepositWithdrawRequest;
import com.example.cgi.dto.ExchangeRequest;
import com.example.cgi.dto.OperationResponse;
import com.example.cgi.exception.AccountNotFoundException;
import com.example.cgi.entity.Account;
import com.example.cgi.entity.AccountBalance;
import com.example.cgi.model.Currency;
import com.example.cgi.repository.AccountBalanceRepository;
import com.example.cgi.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository balanceRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    public AccountService(
            AccountRepository accountRepository,
            AccountBalanceRepository balanceRepository,
            ExchangeRateProvider exchangeRateProvider
    ) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Transactional(readOnly = true)
    public List<AccountBalance> getBalances(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException();
        }

        return balanceRepository.findAllByAccount_Id(accountId);
    }

    @Transactional
    public OperationResponse deposit(Long accountId, DepositWithdrawRequest request) {
        if (request.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        AccountBalance balance = balanceRepository
                .findForUpdateByAccount_IdAndCurrency(accountId, request.currency())
                .orElseGet(() -> new AccountBalance(
                        account, request.currency()
                ));

        balance.deposit(request.amount());

        balanceRepository.save(balance);

        return new OperationResponse(
                true,
                "Deposit completed successfully",
                new OperationResponse.OperationDetails(
                        "DEPOSIT",
                        request.currency(),
                        request.amount(),
                        null, null, null, null, null
                ),
                getBalances(accountId)
        );
    }

    @Transactional
    public OperationResponse withdraw(Long accountId, DepositWithdrawRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        AccountBalance balance = balanceRepository
                .findForUpdateByAccount_IdAndCurrency(accountId, request.currency())
                .orElseThrow(AccountNotFoundException::new);

        balance.withdraw(request.amount());

        balanceRepository.save(balance);

        return new OperationResponse(
                true,
                "Withdraw completed successfully",
                new OperationResponse.OperationDetails(
                        "WITHDRAW",
                        request.currency(),
                        request.amount(),
                        null, null, null, null, null
                ),
                getBalances(accountId)
        );
    }

    @Transactional
    public OperationResponse exchange(Long accountId, ExchangeRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        if (request.currencyFrom() == request.currencyTo()){
            throw new IllegalArgumentException(
                    "Source and target currencies should be different, same given"
            );
        }

        // Always acquire locks in the same order to prevent deadlocks.
        Currency firstCurrency = request.currencyFrom()
                .compareTo(request.currencyTo()) < 0
                ? request.currencyFrom()
                : request.currencyTo();

        Currency secondCurrency = firstCurrency == request.currencyFrom()
                ? request.currencyTo()
                : request.currencyFrom();

        AccountBalance firstBalance = balanceRepository
                .findForUpdateByAccount_IdAndCurrency(accountId, firstCurrency)
                .orElseThrow(AccountNotFoundException::new);

        AccountBalance secondBalance = balanceRepository
                .findForUpdateByAccount_IdAndCurrency(accountId, secondCurrency)
                .orElseThrow(AccountNotFoundException::new);

        // Restore the requested exchange direction.
        AccountBalance balanceFrom = request.currencyFrom() == firstCurrency
                ? firstBalance
                : secondBalance;

        AccountBalance balanceTo = request.currencyTo() == firstCurrency
                ? firstBalance
                : secondBalance;

        BigDecimal rate = exchangeRateProvider.rate(
                request.currencyFrom(),
                request.currencyTo()
        );

        balanceFrom.withdraw(request.amount());

        BigDecimal resultAmount = request.amount()
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        balanceTo.deposit(resultAmount);

        balanceRepository.save(balanceFrom);
        balanceRepository.save(balanceTo);

        return new OperationResponse(
                true,
                "Currency exchange completed successfully",
                new OperationResponse.OperationDetails(
                        "EXCHANGE",
                        null,
                        null,
                        request.currencyFrom(),
                        request.amount(),
                        request.currencyTo(),
                        resultAmount,
                        rate
                ),
                getBalances(accountId)
        );
    }
}