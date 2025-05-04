package com.team1.mixIt.user.service;

import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.exception.UserNotFoundException;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(String loginId, String pwd, String inputPwd) {
        if (!StringUtils.equals(pwd, inputPwd)) throw new RuntimeException(); // Todo Exception 정의
        return userRepository.findByLoginId(loginId).orElseThrow(() -> new UserNotFoundException(loginId));
    }

    @Transactional
    public void updateNotificationSettings(Long userId, boolean eventOn, boolean alertOn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        user.setNotifyOn(eventOn);
        user.setPushOn(alertOn);
        userRepository.save(user);
    }
}
