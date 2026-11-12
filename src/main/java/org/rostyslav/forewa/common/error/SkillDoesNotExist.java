package org.rostyslav.forewa.common.error;

public class SkillDoesNotExist extends RuntimeException {
    public SkillDoesNotExist() {
        super("Skill does not exist");
    }
    public SkillDoesNotExist(String message) {
        super(message);
    }
}
