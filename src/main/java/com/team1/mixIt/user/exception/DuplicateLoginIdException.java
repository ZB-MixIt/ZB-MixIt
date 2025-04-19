package com.team1.mixIt.user.exception;

public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException(String loginId) {
        super(loginId);
    }
}
