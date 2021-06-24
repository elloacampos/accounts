package com.db.awmd.challenge.exception;

/**
 * Exception to be thrown if there is any issue during money transaction
 *
 */
public class TransactionException extends RuntimeException {

	public TransactionException(String message) {
		super(message);
	}
	
}