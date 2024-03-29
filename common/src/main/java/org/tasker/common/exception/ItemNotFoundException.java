package org.tasker.common.exception;

public class ItemNotFoundException extends ExpectedException {
    public ItemNotFoundException() {
        super("Item not found");
    }

    public ItemNotFoundException(String message, Object... args) {
        super(message, args);
    }
}
