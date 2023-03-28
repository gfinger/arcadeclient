package org.makkiato.arcadeclient.data.exception.client;

public class ConversionException extends ArcadeClientException {
    public ConversionException(String message, Throwable reason) {
        super(message, reason);
    }

    public ConversionException(String message) {
        super(message);
    }
}
