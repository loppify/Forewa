package org.rostyslav.forewa.common.error;

public class AccountDoesNotExistException extends RuntimeException {
    public AccountDoesNotExistException() {
        super("Account does not exist");
    }
}
