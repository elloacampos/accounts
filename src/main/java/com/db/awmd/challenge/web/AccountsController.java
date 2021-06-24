package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.dto.AccountDto;
import com.db.awmd.challenge.domain.dto.TransferDto;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransactionException;
import com.db.awmd.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;
	
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid AccountDto accountDto) {
		log.info("Creating account {}", accountDto);
		try {
			this.accountsService.createAccount(new Account(accountDto));
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@PutMapping
	public ResponseEntity<Object> transferMoneyBetweenAccounts(
			@RequestBody @Valid TransferDto transfer) {
		log.info("Transferring money between accounts {}", transfer);
		try {
			//checking if accounts are different
			if (transfer.getAccountFrom().equals(transfer.getAccountTo())) {
				throw new TransactionException("Accounts must be different.");
			}
			//make transaction
			this.accountsService.transfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
		} catch (AccountNotFoundException | TransactionException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = "/{accountId}")
	public AccountDto getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		Account account = this.accountsService.getAccount(accountId);
		return new AccountDto(account.getAccountId(), account.getBalance());
	}

}