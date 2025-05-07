package com.team1.mixIt.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {// Success
    SUCCESS                             ("000_000", HttpStatus.OK,          "Success"),
    INVALID_REQUEST                     ("100_000", HttpStatus.BAD_REQUEST, "Invalid request"),

    // 인증 관련
    BAD_CREDENTIAL                      ("101_000", HttpStatus.BAD_REQUEST, "Bad credentials"),
    UNAUTHORIZED                        ("101_001", HttpStatus.UNAUTHORIZED,"Unauthorized"),
    FORBIDDEN                           ("101_002", HttpStatus.FORBIDDEN,   "Forbidden"),
    INVALID_TOKEN                       ("101_003", HttpStatus.UNAUTHORIZED,"Invalid or expired token"),
    PASSWORD_MISMATCH                   ("101_004", HttpStatus.BAD_REQUEST, "Password mismatch"),

    // 회원 관련
    USER_NOT_FOUND                      ("200_000", HttpStatus.BAD_REQUEST, "User not found"),
    DUPLICATE_LOGIN_ID                  ("200_001", HttpStatus.BAD_REQUEST, "Duplicate Login Id"),
    DUPLICATE_EMAIL                     ("200_002", HttpStatus.BAD_REQUEST, "Duplicate Email"),
    DUPLICATE_NICKNAME                  ("200_003", HttpStatus.BAD_REQUEST, "Duplicate Nickname"),

    EMAIL_NOT_VERIFIED                  ("200_100", HttpStatus.BAD_REQUEST, "Email not verified"),
    EMAIL_VERIFICATION_HISTORY_NOT_FOUND("200_101", HttpStatus.BAD_REQUEST, "Email verification history not found"),
    EMAIL_VERIFICATION_CODE_NOT_MATCHED ("200_102", HttpStatus.BAD_REQUEST, "Email verification code not match"),

    TERMS_NOT_FOUND                     ("200_200", HttpStatus.BAD_REQUEST, "Terms not found"),
    REQUIRED_TERMS_NOT_PROVIDED         ("200_201", HttpStatus.BAD_REQUEST, "Required terms not provided"),

    // 게시글 관련
    POST_NOT_FOUND                      ("300_000", HttpStatus.NOT_FOUND, "Post not found"),
    REVIEW_NOT_FOUND                    ("301_000", HttpStatus.NOT_FOUND, "Review not found"),

    // 이미지 관련
    IMAGE_NOT_FOUND                     ("400_001", HttpStatus.BAD_REQUEST, "Image not found"),
    IMAGE_OWNER_ALREADY_EXIST           ("400_002", HttpStatus.BAD_REQUEST, "Image owner already exists"),

    INTERNAL_SERVER_ERROR               ("500_000", HttpStatus.INTERNAL_SERVER_ERROR,   "Internal Server Error");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}