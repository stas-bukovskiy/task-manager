package org.tasker.task.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException() {
        super("Board not found");
    }
}
