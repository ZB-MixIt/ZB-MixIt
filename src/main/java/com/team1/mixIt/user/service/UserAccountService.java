package com.team1.mixIt.user.service;

import com.team1.mixIt.email.service.EmailService;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.exception.ImageNotFoundException;
import com.team1.mixIt.image.exception.ImageOwnerAlreadyExistException;
import com.team1.mixIt.image.repository.ImageRepository;
import com.team1.mixIt.term.service.UserTermsService;
import com.team1.mixIt.user.dto.UserCreateDto;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.exception.*;
import com.team1.mixIt.user.repository.UserRepository;
import com.team1.mixIt.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final EmailService emailService;
    private final UserTermsService userTermsService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final ImageRepository imageRepository;


    public Optional<User> findUser(String name, String email, String birth) {
        return userRepository.findByNameAndEmailAndBirthdate(name, email, convertToLocalDate(birth));
    }

    @Transactional
    public User createUser(UserCreateDto dto) {
        userRepository.findByLoginId(dto.getLoginId()).ifPresent(v -> {throw new DuplicateLoginIdException(dto.getLoginId());});
        userRepository.findByEmail(dto.getEmail()).ifPresent(v -> {throw new DuplicateEmailException(dto.getEmail());});
        userRepository.findByNickname(dto.getNickname()).ifPresent(v -> {throw new DuplicateNicknameException(dto.getNickname());});

        // emailService.checkIsEmailVerified(dto.getEmail());
        userTermsService.checkRequiredTerms(dto.getTerms());

        User user = User.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .birthdate(convertToLocalDate(dto.getBirth()))
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .build();

        if (Objects.nonNull(dto.getImageId())) {
            Image image = imageRepository.findById(dto.getImageId()).orElseThrow(ImageNotFoundException::new);

            if (Objects.nonNull(image.getUser())) throw new ImageOwnerAlreadyExistException();
            user.updateProfileImage(image);
        }

        user = userRepository.save(user);
        userTermsService.agreeTerms(dto.getTerms(), user);
        emailService.deleteVerifiedHistory(dto.getEmail());
        return user;
    }

    public Boolean checkDuplicate(String loginId, String nickname, String email) {
        if (userRepository.findByLoginId(loginId).isPresent()) {
            return true;
        } else if (userRepository.findByNickname(nickname).isPresent()) {
            return true;
        } else return userRepository.findByEmail(email).isPresent();
    }

    public void updatePassword(String loginId, String oldPwd, String newPwd) {
        User user = userRepository.findByLoginId(loginId).orElseThrow();

        if (!passwordEncoder.encode(oldPwd).equals(user.getPassword())) throw new PasswordMismatchException(oldPwd);

        user.updatePassword(passwordEncoder.encode(newPwd));
        userRepository.save(user);
    }

    public void resetPassword(String loginId, String birth, String email) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UserNotFoundException(loginId));

        if (!DateUtils.yyMMdd(user.getBirthdate()).equals(birth)) throw new UserNotFoundException(birth);
        if (!user.getEmail().equals(email)) throw new UserNotFoundException(email);

        String newPwd = UUID.randomUUID().toString().substring(0, 8);

        emailService.sendPasswordResetEmail(user.getEmail(), newPwd);
        updatePassword(loginId, user.getPassword(), newPwd);
    }

    private LocalDate convertToLocalDate(String dateStr) {
        int yy = Integer.parseInt(dateStr.substring(0, 2));
        int mm = Integer.parseInt(dateStr.substring(2, 4));
        int dd = Integer.parseInt(dateStr.substring(4, 6));

        // Todo Exception 발생 가능성 존재 DateTimeException
        return LocalDate.of((yy > 50) ? (1900+yy) : 2000 + yy, mm, dd);
    }
}
