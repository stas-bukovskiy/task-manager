package org.tasker.common.exception;

public class ExpectedException extends RuntimeException {
    public ExpectedException(String message, Object... args) {
        super(String.format(message, args));
    }
}
