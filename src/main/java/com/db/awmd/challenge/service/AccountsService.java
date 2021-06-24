package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;
	
	private final EmailNotificationService emailNotificationService;
	
	private final Lock lock = new ReentrantLock();

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,
			EmailNotificationService emailNotificationService) {
		this.accountsRepository = accountsRepository;
		this.emailNotificationService = emailNotificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(String accountIdFrom, String accountIdTo, BigDecimal amount) {
		lock.lock();
		try {
			// get accounts
			Account accountFrom = getAccount(accountIdFrom);
			Account accountTo = getAccount(accountIdTo);
			
			// transfer money
			this.accountsRepository.transfer(accountFrom, accountTo, amount);
			
			// send notification to both accounts
			this.emailNotificationService.notifyAboutTransfer(accountTo, amount + " sent from " + accountFrom.getAccountId());
			this.emailNotificationService.notifyAboutTransfer(accountFrom, amount + " sent to " + accountTo.getAccountId());
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();	
		}
	}

}