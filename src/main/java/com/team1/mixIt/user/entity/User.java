package com.team1.mixIt.user.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.common.entity.Image;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity implements UserDetails  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;

    private String password;

    private String nickname;

    private String name;

    private LocalDate birthdate;

    private String email;

    private String phoneNumber;

    private boolean phoneVerified;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    private String social;

    private String socialUserId;

    private String socialLink;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public void updatePassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
