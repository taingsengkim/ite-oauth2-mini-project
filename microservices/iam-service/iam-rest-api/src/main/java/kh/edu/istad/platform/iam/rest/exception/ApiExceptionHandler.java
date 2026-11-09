package kh.edu.istad.platform.iam.rest.exception;

import kh.edu.istad.platform.iam.application.exception.AuthenticationRequiredException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice(basePackages = "kh.edu.istad.platform.iam.rest")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceConflictException.class)
    public ProblemDetail handleResourceConflict(ResourceConflictException exception, WebRequest request) {
        return problem(HttpStatus.CONFLICT, "conflict", "Conflict", exception.getMessage(), "CONFLICT", request);
    }

    @ExceptionHandler(IamDomainException.class)
    public ProblemDetail handleIamDomainException(IamDomainException exception, WebRequest request) {
        HttpStatus status = exception.getCode() == IamDomainException.Code.VALIDATION_ERROR
                ? HttpStatus.BAD_REQUEST : HttpStatus.CONFLICT;
        return problem(status, status == HttpStatus.BAD_REQUEST ? "validation-error" : "rule-violation",
                status == HttpStatus.BAD_REQUEST ? "Validation failed" : "Conflict",
                exception.getMessage(), exception.getCode().name(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception, WebRequest request) {
        var problem = problem(HttpStatus.NOT_FOUND, "not-found", "Not Found",
                exception.getMessage(), "NOT_FOUND", request);
        problem.setProperty("resource", exception.getResource());
        problem.setProperty("resourceUuid", exception.getResourceUuid());
        return problem;
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationRequired(
            AuthenticationRequiredException exception, WebRequest request) {
        var problem = problem(HttpStatus.UNAUTHORIZED, "authentication-required", "Unauthorized",
                exception.getMessage(), "AUTHENTICATION_REQUIRED", request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = new ArrayList<FieldError>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(new FieldError(error.getField(), message(error.getDefaultMessage()))));
        exception.getBindingResult().getGlobalErrors().forEach(error ->
                errors.add(new FieldError("request", message(error.getDefaultMessage()))));
        return validation(exception, headers, status, request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = new ArrayList<FieldError>();
        exception.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> errors.add(
                    new FieldError(name == null ? "request" : name, message(error.getDefaultMessage()))));
        });
        return validation(exception, headers, status, request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return validation(exception, headers, status, request,
                List.of(new FieldError("body", "must contain valid JSON with valid field types")));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String field = exception instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName() : "request";
        return validation(exception, headers, status, request,
                List.of(new FieldError(field, "has an invalid format or value")));
    }

    private ResponseEntity<Object> validation(Exception exception, HttpHeaders headers,
                                               HttpStatusCode status, WebRequest request, List<FieldError> errors) {
        var problem = problem(status, "validation-error", "Validation failed",
                "One or more request fields are invalid", "VALIDATION_ERROR", request);
        problem.setProperty("errors", List.copyOf(errors));
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    private ProblemDetail problem(HttpStatusCode status, String type, String title,
                                  String detail, String code, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://iam.istad.co/problems/" + type));
        problem.setTitle(title);
        problem.setProperty("code", code);
        if (request instanceof ServletWebRequest servlet) {
            problem.setInstance(URI.create(servlet.getRequest().getRequestURI()));
        }
        return problem;
    }

    private String message(String value) {
        return value == null ? "is invalid" : value;
    }

    public record FieldError(String field, String message) {}
}
