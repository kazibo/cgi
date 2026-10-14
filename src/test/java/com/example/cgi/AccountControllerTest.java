package com.example.cgi;

import com.example.cgi.client.ExternalLoggingClient;
import com.example.cgi.controller.AccountController;
import com.example.cgi.dto.DepositRequest;
import com.example.cgi.dto.ExchangeRequest;
import com.example.cgi.dto.OperationResponse;
import com.example.cgi.entity.AccountBalance;
import com.example.cgi.exception.AccountNotFoundException;
import com.example.cgi.exception.InsufficientFundsException;
import com.example.cgi.model.Currency;
import com.example.cgi.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.given;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private ExternalLoggingClient loggingClient;

    @Test
    void getBalanceReturnsAccountIdAndCurrencyBalances() throws Exception {
        AccountBalance eur = mock(AccountBalance.class);
        given(eur.getCurrency()).willReturn(Currency.EUR);
        given(eur.getAmount()).willReturn(new BigDecimal("100.00"));

        AccountBalance usd = mock(AccountBalance.class);
        given(usd.getCurrency()).willReturn(Currency.USD);
        given(usd.getAmount()).willReturn(new BigDecimal("50.00"));

        given(accountService.getBalances(1L)).willReturn(List.of(eur, usd));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.balances.EUR").value(100.00))
                .andExpect(jsonPath("$.balances.USD").value(50.00));

        verify(accountService).getBalances(1L);
    }

    @Test
    void depositReturnsOperationResponse() throws Exception {
        given(accountService.deposit(eq(1L), any(DepositRequest.class)))
                .willReturn(new OperationResponse(
                        true,
                        "Deposit completed successfully",
                        new OperationResponse.OperationDetails(
                                "DEPOSIT", Currency.EUR,
                                new BigDecimal("100.00"),
                                null, null, null, null, null
                        ),
                        List.of()
                ));

        mockMvc.perform(post("/api/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "EUR",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Deposit completed successfully"))
                .andExpect(jsonPath("$.operation.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.operation.currency").value("EUR"))
                .andExpect(jsonPath("$.operation.amount").value(100.00));

        verify(accountService).deposit(eq(1L), any(DepositRequest.class));
    }

    @Test
    void depositWithNegativeAmountReturnsBadRequest() throws Exception {
        given(accountService.deposit(eq(1L), any(DepositRequest.class)))
                .willThrow(new IllegalArgumentException("Amount must be positive"));

        mockMvc.perform(post("/api/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "EUR",
                                  "amount": -10.00
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdrawReturnsOperationResponseAndLogsWithdrawal() throws Exception {
        given(accountService.withdraw(eq(1L), any(DepositRequest.class)))
                .willReturn(new OperationResponse(
                        true,
                        "Withdraw completed successfully",
                        new OperationResponse.OperationDetails(
                                "WITHDRAW", Currency.EUR,
                                new BigDecimal("25.00"),
                                null, null, null, null, null
                        ),
                        List.of()
                ));

        mockMvc.perform(post("/api/accounts/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "EUR",
                                  "amount": 25.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Withdraw completed successfully"))
                .andExpect(jsonPath("$.operation.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.operation.currency").value("EUR"))
                .andExpect(jsonPath("$.operation.amount").value(25.00));

        verify(loggingClient).logWithdrawal();
        verify(accountService).withdraw(eq(1L), any(DepositRequest.class));
    }

    @Test
    void withdrawWithInsufficientFundsReturnsError() throws Exception {
        given(accountService.withdraw(eq(1L), any(DepositRequest.class)))
                .willThrow(new InsufficientFundsException());

        mockMvc.perform(post("/api/accounts/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "EUR",
                                  "amount": 1000.00
                                }
                                """))
                .andExpect(status().isUnprocessableContent());

        verify(loggingClient).logWithdrawal();
        verify(accountService).withdraw(eq(1L), any(DepositRequest.class));
    }

    @Test
    void exchangeReturnsConvertedAmountAndRate() throws Exception {
        given(accountService.exchange(eq(1L), any(ExchangeRequest.class)))
                .willReturn(new OperationResponse(
                        true,
                        "Currency exchange completed successfully",
                        new OperationResponse.OperationDetails(
                                "EXCHANGE", null, null,
                                Currency.EUR, new BigDecimal("100.00"),
                                Currency.USD, new BigDecimal("110.00"),
                                new BigDecimal("1.10")
                        ),
                        List.of()
                ));

        mockMvc.perform(post("/api/accounts/1/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currencyFrom": "EUR",
                                  "currencyTo": "USD",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Currency exchange completed successfully"))
                .andExpect(jsonPath("$.operation.type").value("EXCHANGE"))
                .andExpect(jsonPath("$.operation.currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.operation.amountFrom").value(100.00))
                .andExpect(jsonPath("$.operation.currencyTo").value("USD"))
                .andExpect(jsonPath("$.operation.amountTo").value(110.00))
                .andExpect(jsonPath("$.operation.rate").value(1.10));

        verify(accountService).exchange(eq(1L), any(ExchangeRequest.class));
    }

    @Test
    void exchangeWithInsufficientFundsReturnsError() throws Exception {
        given(accountService.exchange(eq(1L), any(ExchangeRequest.class)))
                .willThrow(new InsufficientFundsException());

        mockMvc.perform(post("/api/accounts/1/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currencyFrom": "EUR",
                                  "currencyTo": "USD",
                                  "amount": 1000.00
                                }
                                """))
                .andExpect(status().isUnprocessableContent());

        verify(accountService).exchange(eq(1L), any(ExchangeRequest.class));
    }

    @Test
    void depositForUnknownAccountReturnsNotFound() throws Exception {
        given(accountService.deposit(eq(999L), any(DepositRequest.class)))
                .willThrow(new AccountNotFoundException());

        mockMvc.perform(post("/api/accounts/999/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "EUR",
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(accountService).deposit(eq(999L), any(DepositRequest.class));
    }
}
