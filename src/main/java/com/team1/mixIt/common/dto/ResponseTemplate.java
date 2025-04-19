package com.team1.mixIt.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team1.mixIt.common.code.ResponseCode;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseTemplate<T> {
    private Status status;
    private T data;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Status {

        private final String code;

        private final String message;
    }

    public static ResponseTemplate<Void> ok() {
        return ResponseTemplate.ok(null);
    }

    public static<T> ResponseTemplate<T> ok(T data) {
        return ResponseTemplate.of(ResponseCode.SUCCESS, data);
    }

    public static ResponseTemplate<Void> of(ResponseCode responseCode) {
        return ResponseTemplate.of(responseCode, null);
    }

    public static <T> ResponseTemplate<T> of(ResponseCode code, T data) {
        return ResponseTemplate.<T>builder()
                .status(
                        Status.builder()
                                .code(code.getCode())
                                .message(code.getMessage())
                                .build()
                )
                .data(data)
                .build();
    }
}