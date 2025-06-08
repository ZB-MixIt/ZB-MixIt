package com.team1.mixIt.user.dto;

import com.team1.mixIt.user.controller.UserAccountController;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCreateDto {
    private String loginId;
    private String password;
    private String name;
    private String birth;
    private String email;
    private String nickname;
    private Long imageId;
    private Boolean notifyOn;
    private Boolean pushOn;
    private List<Integer> terms;

    public static UserCreateDto of(UserAccountController.CreateUserRequest request) {
        return UserCreateDto.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .birth(request.getBirth())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .imageId(request.getImageId())
                .terms(request.getTerms())
                .notifyOn(Objects.isNull(request.getNotifyOn()) ? false : request.getNotifyOn())
                .pushOn(!Objects.isNull(request.getPushOn()) && request.getNotifyOn())
                .build();
    }
}
