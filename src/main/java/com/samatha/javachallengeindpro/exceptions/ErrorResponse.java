package com.samatha.javachallengeindpro.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private int statusCode;
    private String errorMessage;

    public ErrorResponse(int statusCode,String message) {
        this.statusCode = statusCode;
        this.errorMessage = message;
    }

    // Constructor, getters, and setters
}
