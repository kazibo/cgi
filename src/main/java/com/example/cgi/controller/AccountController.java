package com.example.cgi.controller;

import com.example.cgi.client.ExternalLoggingClient;
import com.example.cgi.dto.BalanceResponse;
import com.example.cgi.dto.DepositRequest;
import com.example.cgi.dto.ExchangeRequest;
import com.example.cgi.dto.OperationResponse;
import com.example.cgi.entity.AccountBalance;
import com.example.cgi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ExternalLoggingClient loggingClient;

    public AccountController(
            AccountService accountService,
            ExternalLoggingClient loggingClient
    ) {
        this.accountService = accountService;
        this.loggingClient = loggingClient;
    }

    @GetMapping("/{id}")
    public BalanceResponse getBalance(
            @PathVariable Long id
    ) {
        return new BalanceResponse(
                id,
                accountService.getBalances(id)
                .stream()
                .collect(
                        Collectors.toMap(
                                AccountBalance::getCurrency,
                                AccountBalance::getAmount
                        )
                )
        );
    }

    @PostMapping("/{id}/deposit")
    public OperationResponse deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request
    ) {
        return accountService.deposit(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public OperationResponse withdraw(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request
    ) {
        loggingClient.logWithdrawal();
        return accountService.withdraw(id, request);
        // possibly should also log withdrawal result (success/failure)
    }

    @PostMapping("/{id}/exchange")
    public OperationResponse exchange(
            @PathVariable Long id,
            @Valid @RequestBody ExchangeRequest request
    ) {
        return accountService.exchange(id, request);
    }

}
