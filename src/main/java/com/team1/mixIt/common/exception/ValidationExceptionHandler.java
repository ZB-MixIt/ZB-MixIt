package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.dto.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class ValidationExceptionHandler {

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseTemplate<Map<String, List<MediaType>>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        return ResponseTemplate.of(ResponseCode.INVALID_REQUEST, Map.of("supportedMediaTypes", ex.getSupportedMediaTypes()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseTemplate<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(fieldName, msg);
        });
        return ResponseTemplate.of(ResponseCode.INVALID_REQUEST, errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseTemplate<Map<String, String>> handleMissingServletExceptions(MissingServletRequestParameterException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getParameterName(), String.format("%s type parameter required.", ex.getParameterType()));
        return ResponseTemplate.of(ResponseCode.INVALID_REQUEST, errors);
    }
}
