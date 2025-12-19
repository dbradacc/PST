package com.adminzone.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " cu ID " + id + " nu a fost găsit");
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " '" + identifier + "' nu a fost găsit");
    }
}
