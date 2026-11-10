package org.rostyslav.forewa.common.error;

public class PathDoesNotExistException extends RuntimeException {
    public PathDoesNotExistException(){
        super("Path does not exist");
    }
}
