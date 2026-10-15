package com.bank.exception;

/** Unchecked exception used for all business-rule and database failures in the bank app. */
public class BankException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
