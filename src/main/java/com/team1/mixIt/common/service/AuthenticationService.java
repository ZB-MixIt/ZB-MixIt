package com.team1.mixIt.common.service;

import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public User authenticate(String loginId, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginId,
                        password
                )
        );

        return userRepository.findByLoginId(loginId).orElseThrow();
    }
}
