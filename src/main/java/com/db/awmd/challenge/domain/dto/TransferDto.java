package com.db.awmd.challenge.domain.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Transfer - Data Transfer Object
 * Used to transfer data to transfer money between accounts
 * 
 */
@Data
public class TransferDto {

	@NotNull
	@NotEmpty
	private String accountFrom;
	
	@NotNull
	@NotEmpty
	private String accountTo;
	
	@NotNull
	@DecimalMin(value = "0.01", message = "Value to be transferred must be positive and greater than $0")
	private BigDecimal amount;
	
	@JsonCreator
	public TransferDto(@JsonProperty("accountFrom") String accountFrom,
			@JsonProperty("accountTo") String accountTo,
			@JsonProperty("amount") BigDecimal amount) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
		this.amount = amount;
	}
	
}