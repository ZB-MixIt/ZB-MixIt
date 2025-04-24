package com.team1.mixIt.common.code;

import com.team1.mixIt.email.exception.EmailNotVerifiedException;
import com.team1.mixIt.email.exception.EmailVerificationCodeNotMatch;
import com.team1.mixIt.email.exception.EmailVerificationHistoryNotFound;
import com.team1.mixIt.user.exception.DuplicateEmailException;
import com.team1.mixIt.user.exception.DuplicateLoginIdException;
import com.team1.mixIt.user.exception.DuplicateNicknameException;
import com.team1.mixIt.user.exception.PasswordMismatchException;
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

    DUPLICATE_LOGIN_ID(
            "200_001",
            "Duplicate Login Id",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    DuplicateLoginIdException.class
            }
    ),

    DUPLICATE_EMAIL(
            "200_002",
            "Duplicate Email",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    DuplicateEmailException.class
            }
    ),

    DUPLICATE_NICKNAME(
            "200_003",
            "Duplicate Nickname",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    DuplicateNicknameException.class
            }
    ),

    PASSWORD_MISMATCH(
            "200_004",
            "Password mismatch",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    PasswordMismatchException.class
            }
    ),

    EMAIL_NOT_VERIFIED(
            "200_100",
            "Email not verified",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    EmailNotVerifiedException.class
            }
    ),

    EMAIL_VERIFICATION_HISTORY_NOT_FOUND(
            "200_101",
            "Email verification history not found",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    EmailVerificationHistoryNotFound.class
            }
    ),

    EMAIL_VERIFICATION_CODE_NOT_MATCHED(
            "200_102",
            "Email verification code not match",
            HttpStatus.BAD_REQUEST,
            new Class[] {
                    EmailVerificationCodeNotMatch.class
            }
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