package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import com.db.awmd.challenge.domain.dto.AccountDto;
import com.db.awmd.challenge.exception.TransactionException;

import lombok.Data;

@Data
public class Account {

	private final String accountId;
	private BigDecimal balance;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	public Account(String accountId, BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	public Account(AccountDto accountDto) {
		this.accountId = accountDto.getAccountId();
		this.balance = accountDto.getBalance();
	}

	public void withdraw(BigDecimal amount) throws TransactionException {
		BigDecimal balance = this.getBalance();
		if (amount.compareTo(balance) == 1) {
			throw new TransactionException("Account " + this.getAccountId() + " has not enought money to make this transaction.");
		}
		this.setBalance(balance.subtract(amount));
	}

	public void deposit(BigDecimal amount) {
		BigDecimal balance = this.getBalance();
		this.setBalance(balance.add(amount));
	}

}