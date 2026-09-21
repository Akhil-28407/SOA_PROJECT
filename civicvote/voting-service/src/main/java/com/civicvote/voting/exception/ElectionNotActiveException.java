package com.civicvote.voting.exception;

public class ElectionNotActiveException extends RuntimeException {
    public ElectionNotActiveException(String message) {
        super(message);
    }
}
