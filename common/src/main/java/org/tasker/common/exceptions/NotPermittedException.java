package org.tasker.common.exceptions;

public class NotPermittedException extends RuntimeException {

    public NotPermittedException() {
        super("Not permitted to perform this action.");
    }

}
