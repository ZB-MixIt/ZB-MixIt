package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.dto.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class AuthenticationExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseTemplate<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        ResponseCode code = ResponseCode.BAD_CREDENTIAL;
        return new ResponseEntity<>(ResponseTemplate.of(code), code.getHttpStatus());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseTemplate<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ResponseCode code = ResponseCode.INVALID_TOKEN;
        return new ResponseEntity<>(ResponseTemplate.of(code), code.getHttpStatus());
    }
}
