package com.example.cgi.dto;

import com.example.cgi.entity.AccountBalance;

import java.util.List;

public record OperationResponse(
        boolean success,
        String message,
        OperationDetails operation,
        List<AccountBalance> balances
) {}
