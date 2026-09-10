package com.careermatch.careermatch_backend.exception;
import org.springframework.http.HttpStatus;
public class InvalidCvException extends ApiException {
    public InvalidCvException(String message) { super(HttpStatus.BAD_REQUEST, message); }
}
