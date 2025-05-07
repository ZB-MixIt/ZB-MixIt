package com.team1.mixIt.user.service;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getMyPage(String loginId, String pwd, String inputPwd) {
        if (!passwordEncoder.matches(inputPwd, pwd)) throw new ClientException(ResponseCode.PASSWORD_MISMATCH);
        return userRepository.findByLoginId(loginId).orElseThrow(() -> new ClientException(ResponseCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateNotificationSettings(Long userId, boolean eventOn, boolean alertOn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(ResponseCode.USER_NOT_FOUND));

        user.setNotifyOn(eventOn);
        user.setPushOn(alertOn);
        userRepository.save(user);
    }
}
