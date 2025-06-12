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
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    private String social;

    private String socialUserId;

    private String socialLink;

    @Column(name = "email_notify", nullable = false)
    @Builder.Default
    private boolean emailNotify = false;

    @Column(name = "sms_notify", nullable = false)
    @Builder.Default
    private boolean smsNotify = false;

    @Column(name = "post_like_alarm", nullable = false)
    @Builder.Default
    private boolean postLikeAlarm = true;

    @Column(name = "post_review_alarm", nullable = false)
    @Builder.Default
    private boolean postReviewAlarm = true;

    @Column(name = "popular_post_alarm", nullable = false)
    @Builder.Default
    private boolean popular_post_alarm = true;


    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public void updateEmailNotify(boolean flag) {
        this.emailNotify = flag;
    }

    public void updateSmsNotify(boolean flag) {
        this.smsNotify = flag;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfileImage(Image image) {
        this.profileImage = image;
    }

    public void updatePostLikeAlarm(boolean flag) {
        this.postLikeAlarm = flag;
    }

    public void updatePostReviewAlarm(boolean flag) {
        this.postReviewAlarm = flag;
    }

    public void updatePopularPostAlarm(boolean flag) {
        this.popular_post_alarm = flag;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
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

    // 편의상 메서드 추가
    public Long getProfileImageId() {
        return profileImage != null ? profileImage.getId() : null;
    }

    public void delete() {
        this.loginId = UUID.randomUUID().toString().substring(0, 10);
        this.password = null;
        this.name = UUID.randomUUID().toString();
        this.email = UUID.randomUUID().toString();
        this.profileImage = null;
        this.social = null;
        this.socialUserId = null;
        this.socialLink = null;
        this.birthdate = null;
        this.nickname = "탈퇴한사용자";
    }
}
