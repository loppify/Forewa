package org.rostyslav.forewa.common.error;

public class NodeTypeViolation extends RuntimeException {
    public NodeTypeViolation() {
        super("TRAINABLE node cannot have children");
    }
}
