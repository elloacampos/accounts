package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransactionException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	private AccountsService accountsService;
	
	@Autowired
	private AccountsRepository accountsRepository;
	
	@Mock
	private EmailNotificationService emailNotificationServiceMock;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.accountsService = new AccountsService(accountsRepository, emailNotificationServiceMock);
		this.accountsRepository.clearAccounts();
	}
	
	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}
	
	@Test
	public void getAccount_failsOnNonexistentAccount() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		try {
			this.accountsService.getAccount(uniqueId);
			fail("Should have failed when getting a nonexistent account");
		} catch (AccountNotFoundException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " not found");
		}
	}
	
	@Test
	public void transfer() {
		Integer time = 9999;
		
		// creating accounts
		Account accountFrom = new Account("a", new BigDecimal(time));
		Account accountTo = new Account("b", BigDecimal.ZERO);

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);
		
		Thread[] threads = new Thread[time];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {
	            public void run() {
	            	accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.ONE);
	            }
	        };
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		assertThat(accountFrom.getBalance()).isEqualByComparingTo("0");
		assertThat(accountTo.getBalance()).isEqualByComparingTo(time.toString());
	}
	
	@Test
	public void transferBetweenSameAccounts() {
		Integer time = 9999;
		Integer midTime = time / 2;
		
		// creating accounts
		Account accountA = new Account("a", new BigDecimal(midTime));
		Account accountB = new Account("b", new BigDecimal(midTime));

		this.accountsService.createAccount(accountA);
		this.accountsService.createAccount(accountB);
		
		Thread[] threads = new Thread[midTime];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {
	            public void run() {
	            	accountsService.transfer(accountA.getAccountId(), accountB.getAccountId(), BigDecimal.ONE);
	            	accountsService.transfer(accountB.getAccountId(), accountA.getAccountId(), BigDecimal.ONE);
	            }
	        };
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		assertThat(accountA.getBalance()).isEqualByComparingTo(midTime.toString());
		assertThat(accountB.getBalance()).isEqualByComparingTo(midTime.toString());
	}
	
	@Test
	public void transfer_failsOnUserHasNoMoneyToTransfer() {
		// creating accounts
		Account accountFrom = new Account("a", BigDecimal.ZERO);
		Account accountTo = new Account("b", BigDecimal.ZERO);

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);

		try {
			accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.ONE);
			fail("Should have failed when transferring money");
		} catch (TransactionException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account " + accountFrom.getAccountId() + " has not enought money to make this transaction.");
		}
	}
	
	@Test
	public void transferMoneyAndEmailWasSent() {
		// creating accounts
		Account accountFrom = new Account("a", BigDecimal.TEN);
		Account accountTo = new Account("b", BigDecimal.TEN);

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);
		
		// transfer money
		this.accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.TEN);
		
		// check
		String message = BigDecimal.TEN + " sent from " + accountFrom.getAccountId();
		Mockito.verify(this.emailNotificationServiceMock).notifyAboutTransfer(accountTo, message);
	}
	
	@Test
	public void transferMoneyThrowsAnExceptionAndEmailWasNotSent() {
		// creating accounts
		Account accountFrom = new Account("a", BigDecimal.ONE);
		Account accountTo = new Account("b", BigDecimal.ONE);

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);
	
		try {
			this.accountsService.transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.TEN);
			fail("Should have failed when transferring money");
		} catch (Exception e) { }
		
		Mockito.verifyNoMoreInteractions(this.emailNotificationServiceMock);
	}
	
}