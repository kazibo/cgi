package com.example.cgi.account;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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

    @PostMapping("/{id}/deposits")
    public ResponseEntity<Void> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request
    ) {
        accountService.deposit(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request
    ) {
        accountService.withdraw(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/exchange")
    public ResponseEntity<Void> exchange(
            @PathVariable Long id,
            @Valid @RequestBody ExchangeRequest request
    ) {
        accountService.exchange(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public String test(){
        return "Test endpoint";
    }

}
