package com.bwc.policymanagement.exception;

public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(String message) {
        super(message);
    }
    
    public PolicyNotFoundException(String resource, String identifier) {
        super(resource + " not found with identifier: " + identifier);
    }
    
    public PolicyNotFoundException(String resource, String identifier1, String identifier2) {
        super(resource + " not found with identifiers: " + identifier1 + ", " + identifier2);
    }
}