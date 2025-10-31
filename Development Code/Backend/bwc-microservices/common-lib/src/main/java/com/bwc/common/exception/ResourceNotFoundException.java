// common-lib/src/main/java/com/bwc/common/exception/ResourceNotFoundException.java
package com.bwc.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier));
    }
    
    public ResourceNotFoundException(String resource, String identifier1, String identifier2) {
        super(String.format("%s not found with identifiers: %s, %s", resource, identifier1, identifier2));
    }
}