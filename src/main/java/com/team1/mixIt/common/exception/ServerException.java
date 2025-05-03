package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;

public class ServerException extends RuntimeException {
    private final ResponseCode code;

    public ServerException(ResponseCode code) {
        this.code = code;
    }

    public ResponseCode getCode() {
        return this.code;
    }
}
