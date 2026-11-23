package org.rostyslav.forewa.common.error;

public class BadRequest extends RuntimeException {
    public BadRequest(String message) {
        super(message);
    }
}
