package org.rostyslav.forewa.common.error;

public class InvalidSkillHierarchy extends RuntimeException {
    public InvalidSkillHierarchy() {
        super();
    }

    public InvalidSkillHierarchy(String message) {
        super(message);
    }
}
