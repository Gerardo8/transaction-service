package com.spin.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the transaction service.
 */
@SpringBootApplication
public class TransactionServiceApplication {

	/** Creates the Spring Boot application entry point. */
	public TransactionServiceApplication() {
	}

	/**
	 * Starts the web service and its configured infrastructure adapters.
	 *
	 * @param args process arguments
	 */
	static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}
}
