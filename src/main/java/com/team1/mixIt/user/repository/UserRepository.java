package com.team1.mixIt.user.repository;

import com.team1.mixIt.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByNameAndEmailAndBirthdate(String loginId, String email, LocalDate birthdate);
}
