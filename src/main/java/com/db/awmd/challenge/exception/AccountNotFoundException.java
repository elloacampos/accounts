package com.db.awmd.challenge.exception;

/**
 * Exception to be thrown if account was not found 
 *
 */
public class AccountNotFoundException extends RuntimeException {

	public AccountNotFoundException(String message) {
		super(message);
	}
	
}