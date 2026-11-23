package org.rostyslav.forewa.common.error;

public class NodeHierarchyCycle extends RuntimeException {
    public NodeHierarchyCycle() {
        super("Invalid node hierarchy");
    }
}
