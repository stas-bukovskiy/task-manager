package org.tasker.common.exceptions;

public class NotPermittedException extends RuntimeException {

    public NotPermittedException() {
        super("Not permitted to perform this action.");
    }

    public NotPermittedException(String message, Object... args) {
        super(String.format(message, args));
    }

}
