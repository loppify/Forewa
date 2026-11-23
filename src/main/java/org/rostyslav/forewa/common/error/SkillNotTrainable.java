package org.rostyslav.forewa.common.error;

public class SkillNotTrainable extends RuntimeException {
    public SkillNotTrainable() {
        super("Skill is not trainable");
    }
}
