package org.rostyslav.forewa.common.error;

public class InvalidGoalTransition extends RuntimeException {
    public InvalidGoalTransition(String message) {
        super(message);
    }
}
