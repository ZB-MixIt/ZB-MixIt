package com.team1.mixIt.user.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.common.service.AuthenticationService;
import com.team1.mixIt.common.service.JwtService;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Login")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "User id, pw 를 바탕으로 로그인 토큰을 발급받는 API")
    public ResponseTemplate<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authenticationService.authenticate(request.getLoginId(), request.getPassword());
        String token = jwtService.generateToken(user);

        return ResponseTemplate.ok(
                LoginResponse.builder()
                        .loginId(user.getLoginId())
                        .name(user.getName())
                        .nickname(user.getNickname())
                        .imageSrc(user.getProfileImage() == null ? null : user.getProfileImage().getUrl())
                        .email(user.getEmail())
                        .birth(DateUtils.yyMMdd(user.getBirthdate()))
                        .token(token)
                        .expiresIn(jwtService.getExpirationTime())
                        .build()
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "User 보유 토큰 비활성화 API"
    )
    public ResponseTemplate<Void> logout() {
        // Todo 입력 토큰에 대한 Blacklist 관리 로직 필요
        return null;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginRequest {
        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String loginId;

        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginResponse {
        private String loginId;
        private String nickname;
        private String email;
        private String imageSrc;
        private String birth;
        private String name;
        private String token;
        private long expiresIn;
    }
}
