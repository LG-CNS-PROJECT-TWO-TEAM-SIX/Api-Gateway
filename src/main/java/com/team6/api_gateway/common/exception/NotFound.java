package com.team6.api_gateway.common.exception;


public class NotFound extends ClientError {
    public NotFound(String errorMessage) {
        this.errorCode = "NotFound";
        this.errorMessage = errorMessage;
    }
}
