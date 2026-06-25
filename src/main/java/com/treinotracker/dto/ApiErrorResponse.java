package com.treinotracker.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(int status, String message) {
        return new ApiErrorResponse(Instant.now(), status, message, null);
    }

    public static ApiErrorResponse of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, message, fieldErrors);
    }
}
