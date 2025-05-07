package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.dto.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(0)
@RestControllerAdvice
@RequiredArgsConstructor
public class DefaultExceptionHandler {

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ResponseTemplate<Void>> handleException(ClientException ex) {
        ResponseCode code = ex.getCode();
        return new ResponseEntity<>(ResponseTemplate.of(code), code.getHttpStatus());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ResponseTemplate<Void>> handleException(ServerException ex) {
        ResponseCode code = ex.getCode();
        return new ResponseEntity<>(ResponseTemplate.of(code), code.getHttpStatus());
    }
}
