package org.tasker.updates.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ItemNotFountException extends ResponseStatusException {

    public ItemNotFountException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
