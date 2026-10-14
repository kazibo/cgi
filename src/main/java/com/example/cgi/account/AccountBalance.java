package com.example.cgi.account;

import com.example.cgi.exceptions.InsufficientFundsException;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "account_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"account_id", "currency"}
        )
)
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    protected AccountBalance() {}

    public AccountBalance(Account account, Currency currency) {
        this.account = account;
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void deposit(BigDecimal amount) {
        this.amount = this.amount.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (this.amount.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        this.amount = this.amount.subtract(amount);
    }
}