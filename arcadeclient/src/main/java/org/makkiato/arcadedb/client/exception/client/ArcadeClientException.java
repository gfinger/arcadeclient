package org.makkiato.arcadedb.client.exception.client;

public class ArcadeClientException extends RuntimeException {
    public ArcadeClientException(String message) {
        super(message);
    }

    public ArcadeClientException(String message, Throwable reason) {
        super(message, reason);
    }
}
