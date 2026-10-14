package com.example.cgi;

import com.example.cgi.dto.DepositRequest;
import com.example.cgi.dto.ExchangeRequest;
import com.example.cgi.entity.Account;
import com.example.cgi.entity.AccountBalance;
import com.example.cgi.exception.InsufficientFundsException;
import com.example.cgi.model.Currency;
import com.example.cgi.repository.AccountBalanceRepository;
import com.example.cgi.repository.AccountRepository;
import com.example.cgi.service.AccountService;
import com.example.cgi.service.ExchangeRateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository balanceRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @Test
    void depositIncreasesBalanceByRequestedAmount() {
        account = mock(Account.class);
        when(accountRepository.existsById(1L)).thenReturn(true);

        // Given: the account exists and has no EUR balance yet
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.empty());

        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of());

        // When: deposit 100 EUR
        accountService.deposit(
                1L,
                new DepositRequest(Currency.EUR, new BigDecimal("100.00"))
        );

        // Then: the saved balance is exactly 100 EUR
        ArgumentCaptor<AccountBalance> captor =
                ArgumentCaptor.forClass(AccountBalance.class);

        verify(balanceRepository).save(captor.capture());

        AccountBalance savedBalance = captor.getValue();

        assertEquals(Currency.EUR, savedBalance.getCurrency());
        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(savedBalance.getAmount())
        );
    }

    @Test
    void depositAddsAmountToExistingBalance() {
        account = mock(Account.class);
        when(accountRepository.existsById(1L)).thenReturn(true);

        // Given: existing EUR balance is 50
        AccountBalance balance =
                new AccountBalance(account, Currency.EUR);

        balance.deposit(new BigDecimal("50.00"));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(balance));

        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of(balance));

        // When: deposit another 30 EUR
        accountService.deposit(
                1L,
                new DepositRequest(Currency.EUR, new BigDecimal("30.00"))
        );

        // Then: balance is 80 EUR
        assertMoneyEquals("80.00", balance.getAmount());
        verify(balanceRepository).save(balance);
    }

    @Test
    void withdrawDecreasesBalanceByRequestedAmount() {
        account = mock(Account.class);
        when(accountRepository.existsById(1L)).thenReturn(true);

        // Given: existing EUR balance is 100
        AccountBalance balance =
                new AccountBalance(account, Currency.EUR);

        balance.deposit(new BigDecimal("100.00"));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(balance));

        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of(balance));

        // When: withdraw 25 EUR
        accountService.withdraw(
                1L,
                new DepositRequest(Currency.EUR, new BigDecimal("25.00"))
        );

        // Then: balance is 75 EUR
        assertMoneyEquals("75.00", balance.getAmount());
        verify(balanceRepository).save(balance);
    }

    @Test
    void withdrawEntireBalanceLeavesZero() {
        account = mock(Account.class);
        when(accountRepository.existsById(1L)).thenReturn(true);

        // Given: existing EUR balance is 100
        AccountBalance balance =
                new AccountBalance(account, Currency.EUR);

        balance.deposit(new BigDecimal("100.00"));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(balance));

        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of(balance));

        // When: withdraw the entire balance
        accountService.withdraw(
                1L,
                new DepositRequest(Currency.EUR, new BigDecimal("100.00"))
        );

        // Then: balance is zero
        assertMoneyEquals("0.00", balance.getAmount());
    }

    @Test
    void withdrawMoreThanBalanceThrowsAndDoesNotChangeBalance() {
        // Given: existing EUR balance is 50
        AccountBalance balance =
                new AccountBalance(account, Currency.EUR);

        balance.deposit(new BigDecimal("50.00"));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(balance));

        // When / Then: withdrawing 60 EUR fails
        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.withdraw(
                        1L,
                        new DepositRequest(
                                Currency.EUR,
                                new BigDecimal("60.00")
                        )
                )
        );

        // The balance must remain unchanged
        assertMoneyEquals("50.00", balance.getAmount());

        // The operation must not save the modified balance
        verify(balanceRepository, never()).save(any(AccountBalance.class));
    }

    @Test
    void depositWithZeroAmountThrowsAndDoesNotSave() {
        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.deposit(
                        1L,
                        new DepositRequest(Currency.EUR, BigDecimal.ZERO)
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(balanceRepository);
    }

    @Test
    void withdrawWithNegativeAmountThrowsAndDoesNotSave() {
        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.withdraw(
                        1L,
                        new DepositRequest(Currency.EUR, new BigDecimal("-10.00"))
                )
        );

        verifyNoInteractions(balanceRepository);
    }


    @Test
    void exchangeTransfersMoneyBetweenCurrencies() {
        AccountBalance eurBalance = new AccountBalance(account, Currency.EUR);
        eurBalance.deposit(new BigDecimal("100.00"));

        AccountBalance usdBalance = new AccountBalance(account, Currency.USD);
        usdBalance.deposit(new BigDecimal("20.00"));

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(eurBalance));
        when(balanceRepository.findForUpdate(1L, Currency.USD))
                .thenReturn(Optional.of(usdBalance));
        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of(eurBalance, usdBalance));
        when(accountRepository.existsById(1L)).thenReturn(true);

        ExchangeRequest request = new ExchangeRequest(
                Currency.EUR,
                Currency.USD,
                new BigDecimal("50.00")
        );

        accountService.exchange(1L, request);

        assertMoneyEquals("50.00", eurBalance.getAmount());

        BigDecimal rate = new ExchangeRateProvider()
                .rate(Currency.EUR, Currency.USD);
        BigDecimal expectedUsd = new BigDecimal("20.00")
                .add(new BigDecimal("50.00").multiply(rate));

        assertMoneyEquals(expectedUsd.toPlainString(), usdBalance.getAmount());

        verify(balanceRepository).save(eurBalance);
        verify(balanceRepository).save(usdBalance);
    }

    @Test
    void exchangeEntireBalanceLeavesSourceBalanceZero() {
        AccountBalance eurBalance = new AccountBalance(account, Currency.EUR);
        eurBalance.deposit(new BigDecimal("100.00"));

        AccountBalance usdBalance = new AccountBalance(account, Currency.USD);

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(eurBalance));
        when(balanceRepository.findForUpdate(1L, Currency.USD))
                .thenReturn(Optional.of(usdBalance));
        when(balanceRepository.findAllByAccount_Id(1L))
                .thenReturn(List.of(eurBalance, usdBalance));
        when(accountRepository.existsById(1L)).thenReturn(true);

        accountService.exchange(
                1L,
                new ExchangeRequest(
                        Currency.EUR,
                        Currency.USD,
                        new BigDecimal("100.00")
                )
        );

        assertMoneyEquals("0.00", eurBalance.getAmount());

        BigDecimal expectedUsd = new BigDecimal("100.00")
                .multiply(new ExchangeRateProvider()
                        .rate(Currency.EUR, Currency.USD));

        assertMoneyEquals(expectedUsd.toPlainString(), usdBalance.getAmount());
    }

    @Test
    void exchangeMoreThanAvailableBalanceThrowsAndDoesNotSave() {
        AccountBalance eurBalance = new AccountBalance(account, Currency.EUR);
        eurBalance.deposit(new BigDecimal("50.00"));

        AccountBalance usdBalance = new AccountBalance(account, Currency.USD);

        when(balanceRepository.findForUpdate(1L, Currency.EUR))
                .thenReturn(Optional.of(eurBalance));
        when(balanceRepository.findForUpdate(1L, Currency.USD))
                .thenReturn(Optional.of(usdBalance));

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.exchange(
                        1L,
                        new ExchangeRequest(
                                Currency.EUR,
                                Currency.USD,
                                new BigDecimal("60.00")
                        )
                )
        );

        assertMoneyEquals("50.00", eurBalance.getAmount());
        assertMoneyEquals("0.00", usdBalance.getAmount());

        verify(balanceRepository, never()).save(any(AccountBalance.class));
    }

    @Test
    void exchangeWithZeroAmountThrowsAndDoesNotSave() {
        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.exchange(
                        1L,
                        new ExchangeRequest(
                                Currency.EUR,
                                Currency.USD,
                                BigDecimal.ZERO
                        )
                )
        );

        verifyNoInteractions(balanceRepository);
        verifyNoInteractions(accountRepository);
    }

    @Test
    void exchangeWithNegativeAmountThrowsAndDoesNotSave() {
        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.exchange(
                        1L,
                        new ExchangeRequest(
                                Currency.EUR,
                                Currency.USD,
                                new BigDecimal("-10.00")
                        )
                )
        );

        verifyNoInteractions(balanceRepository);
        verifyNoInteractions(accountRepository);
    }


    private void assertMoneyEquals(String expected, BigDecimal actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                "Unexpected balance"
        );
    }
}
