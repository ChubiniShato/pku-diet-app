package com.chubini.pku.norms;

/**
 * Exception thrown when a norm prescription is not found
 */
public class NormNotFoundException extends RuntimeException {
    
    public NormNotFoundException(String message) {
        super(message);
    }
    
    public NormNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
