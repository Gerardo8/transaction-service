package com.spin.transactions.infrastructure.exception;

import com.spin.transactions.domain.exception.BusinessRuleViolationException;
import com.spin.transactions.domain.exception.DomainException;
import com.spin.transactions.domain.exception.InvalidTransactionStateException;
import com.spin.transactions.domain.exception.TransactionLimitExceededException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates validation, business, and unexpected exceptions into HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Creates the exception mapper. */
    public GlobalExceptionHandler() {
    }

    /**
     * Maps transaction amount-limit violations to HTTP 422.
     *
     * @param ex business limit exception
     * @return error response with the rule description
     */
    @ExceptionHandler(TransactionLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleLimitExceeded(TransactionLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps invalid domain state transitions to HTTP 409.
     *
     * @param ex invalid lifecycle transition
     * @return error response with the transition description
     */
    @ExceptionHandler(InvalidTransactionStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidState(InvalidTransactionStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps domain business-rule violations to HTTP 422.
     *
     * @param ex business rule exception
     * @return error response with the rule description
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps remaining domain exceptions to HTTP 422.
     *
     * @param ex domain-level exception
     * @return error response with the domain message
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps invalid query or request arguments to HTTP 400.
     *
     * @param ex invalid argument
     * @return error response with the validation detail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Returns field-specific errors for bean validation failures.
     *
     * @param ex validation exception and field errors
     * @return response containing one message per invalid field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Maps malformed JSON and missing request headers to HTTP 400.
     *
     * @param ex malformed request or binding failure
     * @return error response describing the invalid request
     */
    @ExceptionHandler({ServletRequestBindingException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> handleMalformedRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage() == null ? "Invalid request" : ex.getMessage()));
    }

    /**
     * Maps method parameter constraint failures to HTTP 400.
     *
     * @param ex violated method parameter constraints
     * @return error response with constraint details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Logs unexpected errors and returns a generic HTTP 500 response.
     *
     * @param ex unhandled request exception
     * @return generic error response that does not expose internal details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unhandled request error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
