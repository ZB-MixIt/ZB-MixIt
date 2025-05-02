package com.team1.mixIt.user.dto;

import com.team1.mixIt.user.controller.UserAccountController;
import lombok.*;

import java.util.List;

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
                .build();
    }
}
