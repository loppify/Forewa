package org.rostyslav.forewa.common.error;

import java.util.UUID;

public class PathDoesNotExistException extends RuntimeException {
    public PathDoesNotExistException(String message, UUID id) {
        super(message + id);
    }
}
