package com.smartev.platform.common;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String, String> validationDetails;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String errorCode, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String errorCode, String message, String path, Map<String, String> validationDetails) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.validationDetails = validationDetails;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationDetails() {
        return validationDetails;
    }

    public void setValidationDetails(Map<String, String> validationDetails) {
        this.validationDetails = validationDetails;
    }
}
