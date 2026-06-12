package com.mycompany.contact_app.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;

/**
 * Global handler for business and technical exceptions, ensuring consistent API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BUSINESS_ERROR = "BUSINESS_VALIDATION_FAILED";

    // Handles 409 (Conflict) scenarios, e.g., duplicate records
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Conflict");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Handles 404 (Not Found) scenarios: e.g., resource ID doesn't exist
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource Not Found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Handles 401 (Unauthorized/Forbidden): e.g., missing tenant claim
    @ExceptionHandler(MissingTenantClaimException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleMissingTenantContext(MissingTenantClaimException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized Context");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Handles general API validation failures (e.g., missing required JSON fields)
    @ExceptionHandler({MethodArgumentNotValidException.class, MissingTenantClaimException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationFailure(Exception ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        String detailMessage = (ex instanceof MethodArgumentNotValidException)
                ? "Invalid request parameters." : ex.getMessage();
        problem.setDetail(detailMessage);
        return problem;
    }

    // Catches all unmapped Runtime exceptions as a last resort
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleAllOtherExceptions(Exception ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred: " + ex.getMessage());
        return problem;
    }
}