package com.team1.mixIt.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    // Success
    SUCCESS(
            "000_000",
            "Success",
            HttpStatus.OK,
            null),

    INVALID_REQUEST(
            "100_000",
            "Invalid request",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    HttpMediaTypeNotSupportedException.class,
                    MethodArgumentNotValidException.class,
                    MissingServletRequestParameterException.class}
    ),

    INTERNAL_SERVER_ERROR(
            "500_000",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
    );


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    private final Class<? extends Exception>[] exceptions;
}