package com.team1.mixIt.user.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.user.dto.UserCreateDto;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.service.UserAccountService;
import com.team1.mixIt.user.validation.RequireOneParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Tag(name = "User Account")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @PostMapping("/duplicate")
    @Operation(
            summary = "Check information duplication",
            description = "다음 중 하나의 값[LoginId, Email, Nickname]에 대한 중복 여부를 검사하는 API"
    )
    public ResponseTemplate<Boolean> checkDuplicate(@Valid @RequestBody CheckDuplicateRequest request) {
        return ResponseTemplate.ok(userAccountService.checkDuplicate(request.getLoginId(), request.getNickname(), request.getEmail()));
    }

    @PostMapping
    @Operation(
            summary = "Sign up",
            description = "회원 가입 API")
    public ResponseTemplate<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userAccountService.createUser(UserCreateDto.of(request));
        return ResponseTemplate.ok(CreateUserResponse.of(user));
    }

    @GetMapping
    @Operation(
            summary = "Find loginId",
            description = "User 이름과 생년월일, Email 로 아이디를 찾는 API")
    public ResponseTemplate<FindLoginIdResponse> findLoginId(@Valid FindLoginIdRequest request) {
        Optional<User> optUser= userAccountService.findUser(request.getName(), request.getEmail(), request.getBirth());
        return ResponseTemplate.ok(FindLoginIdResponse.of(optUser.map(User::getLoginId).orElse(null)));
    }

    @PutMapping("/password")
    @Operation(
            summary = "Update pwd",
            description = "User 의 pwd 를 변경하는 API"
    )
    public ResponseTemplate<Void> changePwd(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody ChangePwdRequest request) {
        userAccountService.updatePassword(user.getLoginId(), request.getOldPwd(), request.getNewPwd());
        return ResponseTemplate.ok();
    }

    @PostMapping("/password/reset")
    @Operation(
            summary = "Reset pwd",
            description = "User 의 ID, 생년월일, Email 로 임시 비밀번호를 발급받는 API"
    )
    public ResponseTemplate<Void> resetPwd(@Valid @RequestBody ResetPwdRequest request) {
        userAccountService.resetPassword(request.getLoginId(), request.getBirth(), request.getEmail());
        return ResponseTemplate.ok();
    }

    @RequireOneParam
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CheckDuplicateRequest {

        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String loginId;

        @Length(min = 2, max = 10)
        private String nickname;

        @Email
        @NotNull
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CreateUserRequest {

        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String loginId;

        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String password;

        private String name;

        @Length(min = 6, max = 6)
        @Pattern(regexp = "^\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$")
        private String birth;

        @Email
        @NotNull
        private String email;

        @Length(max = 10)
        private String nickname;

        @Nullable
        private Long imageId;

        @NotNull
        private List<Integer> terms;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CreateUserResponse {
        private String loginId;

        public static CreateUserResponse of(User user) {
            return CreateUserResponse.builder()
                    .loginId(user.getLoginId())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FindLoginIdRequest {
        @NotBlank
        private String name;

        @Length(min = 6, max = 6)
        @Pattern(regexp = "^\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$")
        private String birth;

        @Email
        @NotNull
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FindLoginIdResponse {
        private String userId;

        public static FindLoginIdResponse of(String loginId) {
            return FindLoginIdResponse.builder()
                    .userId(Objects.isNull(loginId) ? null : loginId.substring(0, 4) + loginId.substring(4).replaceAll(".", "*"))
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ResetPwdRequest {
        private String loginId;

        @Length(min = 6, max = 6)
        @Pattern(regexp = "^\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$")
        private String birth;

        @Email
        @NotNull
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChangePwdRequest {
        private String oldPwd;
        private String newPwd;
    }
}
