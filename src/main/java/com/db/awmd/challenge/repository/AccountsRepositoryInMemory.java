package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransactionException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) throws AccountNotFoundException {
		Account account = accounts.get(accountId);
		// if there is no account with this id, it will throw an exception
		if (account == null) {
			throw new AccountNotFoundException("Account id " + accountId + " not found");
		}
		return account;
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public void transfer(Account accountFrom, Account accountTo, BigDecimal amount) throws TransactionException {
		accountFrom.withdraw(amount);
		accountTo.deposit(amount);
	}

}