package org.rostyslav.forewa.common.error;

public class NodeHierarchyRecursion extends RuntimeException {
    public NodeHierarchyRecursion() {
        super("Invalid node hierarchy");
    }
}
