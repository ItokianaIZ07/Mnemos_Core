package com.mnemos.exception;

public class RoleNotAllowed extends RuntimeException {
    public RoleNotAllowed(String message) {
        super(message);
    }
}
