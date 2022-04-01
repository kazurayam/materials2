package com.kazurayam.materialstore;

public final class MaterialstoreException extends Exception {
    public MaterialstoreException(String message) {
        super(message);
    }

    public MaterialstoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MaterialstoreException(Throwable throwable) {
        super(throwable);
    }
}
