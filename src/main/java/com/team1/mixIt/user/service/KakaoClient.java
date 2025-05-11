package com.team1.mixIt.user.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoClient {
    private final RestClient restClient;
    private final String client;
    private final String redirectUrl;
    private final String secret;

    public KakaoClient(
            @Value("${kakao.auth.client}") String client,
            @Value("${kakao.auth.redirect}") String redirectUrl,
            @Value("${kakao.auth.secret}") String secret
    ) {
        this.restClient = RestClient.builder()
                .build();

        this.client = client;
        this.redirectUrl = redirectUrl;
        this.secret = secret;
    }

    public ResponseEntity<Void> requestLogin() {
        return ResponseEntity.status(HttpStatus.valueOf(302)).header(HttpHeaders.LOCATION, String.format("https://kauth.kakao.com/oauth/authorize?response_type=%s&redirect_uri=%s&client_id=%s",
                "code",
                redirectUrl,
                client)).build();
        /*return restClient.get()
                .uri(builder -> builder.scheme("https")
                        .host("kauth.kakao.com")
                        .path("/oauth/authorize")
                        .queryParam("client_id", client)
                        .queryParam("redirect_url", redirectUrl)
                        .queryParam("response_type", "code")
                        .build())
                .retrieve()
                .toBodilessEntity();*/
    }

    public TokenResponse requestToken(String code) {
        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(String.format("grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                                client,
                                secret,
                                code,
                                redirectUrl)
                )
                .retrieve()
                .body(TokenResponse.class);
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfo.class);
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoUserInfo(
            Long id,
            Boolean hasSignedUp,
            KakaoAccount kakaoAccount
    ) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoAccount(
            Boolean profileNeedsAgreement,
            Boolean profileNicknameNeedsAgreement,
            Boolean profileImageNeedsAgreement,
            Profile profile,
            Boolean nameNeedsAgreement,
            String name,
            Boolean emailNeedsAgreement,
            Boolean isEmailValid,
            Boolean isEmailVerified,
            String email
    ) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Profile(
            String nickname,
            String thumbnailImageUrl,
            String profileImageUrl,
            Boolean isDefaultImage,
            Boolean isDefaultNickname
    ) {}

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TokenRequest {
        private String grantType;
        private String clientId;
        private String redirectUri;
        private String code;
        private String clientSecret;

        @Builder
        public TokenRequest(String clientId, String redirectUri, String code, String clientSecret) {
            grantType = "authorization_code";
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.code = code;
            this.clientSecret = clientSecret;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TokenResponse {
        private String tokenType;
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private Integer refreshTokenExpiresIn;
        private String scope;
    }
}
