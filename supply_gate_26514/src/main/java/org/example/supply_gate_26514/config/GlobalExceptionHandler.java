package org.example.supply_gate_26514.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;

/**
 * Global exception handler for handling multipart file upload errors
 * and other common exceptions across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles multipart file size exceeded exceptions.
     * This occurs when file size exceeds Spring Boot's configured limits.
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<?> handleMultipartException(MultipartException ex) {
        System.err.println("ERROR: MultipartException caught: " + ex.getMessage());
        ex.printStackTrace();
        
        // Determine error message based on exception type
        // Use final variables for use in anonymous inner class
        final String errorMessage;
        final String exceptionClassName = ex.getClass().getSimpleName();
        
        if (ex instanceof MaxUploadSizeExceededException) {
            errorMessage = "File size exceeds maximum allowed size. Maximum file size is 10MB per file and 50MB total.";
        } else {
            errorMessage = "File size too large. Maximum file size is 10MB per file and 50MB total.";
        }
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new HashMap<String, String>() {{
                    put("error", "File size too large");
                    put("message", errorMessage);
                    put("details", exceptionClassName);
                }});
    }
}
