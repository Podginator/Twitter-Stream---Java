package com.podginator.twitter.notifier.model;

public class TwitterDisconnectException extends Exception {

    public TwitterDisconnectException() {
        super();
    }

    public TwitterDisconnectException(final String message) {
        super(message);
    }

    public TwitterDisconnectException(final String message, final Throwable t) {
        super(message, t);
    }

    public TwitterDisconnectException(final Throwable t) {
        super(t);
    }
}