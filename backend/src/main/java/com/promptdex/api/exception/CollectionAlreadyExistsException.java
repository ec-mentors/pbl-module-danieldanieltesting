package com.promptdex.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CollectionAlreadyExistsException extends RuntimeException {
    public CollectionAlreadyExistsException(String message) {
        super(message);
    }
}