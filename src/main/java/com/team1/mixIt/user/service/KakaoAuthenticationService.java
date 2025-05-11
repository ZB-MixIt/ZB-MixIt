package com.team1.mixIt.user.service;

import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(KakaoAuthenticationService.class);

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;

    public ResponseEntity<Void> requestLogin() {
        return kakaoClient.requestLogin();
    }

    public User getOrCreateUser(String code) {
        KakaoClient.TokenResponse tokenResponse = kakaoClient.requestToken(code);
        KakaoClient.KakaoUserInfo userInfo = kakaoClient.getUserInfo(tokenResponse.getAccessToken());

        Optional<User> optUser = userRepository.findByEmail(userInfo.kakaoAccount().email());

        if (optUser.isPresent()) {
            log.info("Kakao user already exists. return existing user");
            return optUser.get();
        }

        String nickname = userInfo.kakaoAccount().profile().nickname() + UUID.randomUUID().toString().substring(0,4);
        User user = User.builder()
                .loginId(userInfo.kakaoAccount().email())
                .name(nickname)
                .nickname(nickname)
                .email(userInfo.kakaoAccount().email())
                .birthdate(LocalDate.now())
                .social("kakao")
                .socialLink("")
                .socialUserId("")
                .build();

        user = userRepository.save(user);
        log.info("Kakao user created. LoginId: {}", user.getLoginId());
        return user;
    }
}
