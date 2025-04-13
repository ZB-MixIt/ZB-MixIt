package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.dto.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseCodeResolver resolver;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseTemplate<Void>> handleException(Exception ex) {
        ResponseCode code = resolver.resolve(ex);

        if (code.equals(ResponseCode.INTERNAL_SERVER_ERROR)) {
            log.error("Unhandled exception", ex);
        }

        return new ResponseEntity<>(ResponseTemplate.of(code), code.getHttpStatus());
    }
}
