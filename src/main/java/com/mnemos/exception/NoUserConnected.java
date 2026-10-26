package com.mnemos.exception;

public class NoUserConnected extends RuntimeException {
    public NoUserConnected(String message) {
        super(message);
    }
}
