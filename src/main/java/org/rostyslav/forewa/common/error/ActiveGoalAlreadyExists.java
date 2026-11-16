package org.rostyslav.forewa.common.error;

public class ActiveGoalAlreadyExists extends RuntimeException {
    public ActiveGoalAlreadyExists(String message) {
        super(message);
    }
}
