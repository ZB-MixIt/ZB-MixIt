package com.team1.mixIt.user.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.image.entity.Image;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    public void updateProfileImage(Image image) {
        this.profileImage = image;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return id.intValue();
    }
}
