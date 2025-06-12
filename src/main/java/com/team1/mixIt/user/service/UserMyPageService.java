package com.team1.mixIt.user.service;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.repository.ImageRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.repository.ReviewRepository;
import com.team1.mixIt.user.controller.UserMyPageController;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserMyPageService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;


    public Page<UserMyPageController.MyReviewDto> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("user").get("id"), userId),
                pageable);

        return reviews.map(r -> {
            List<Image> images = imageService.findAllById(r.getImageIds());
            return UserMyPageController.MyReviewDto.of(r, images);
        });
    }

    @Transactional
    public User updateMyPage(User user, UserMyPageController.UpdateMyPageRequest dto) {
        user = userRepository.findById(user.getId()).orElseThrow(() -> new ClientException(ResponseCode.USER_NOT_FOUND));

        if (!user.getNickname().equals(dto.nickname())) {
            userRepository.findByNickname(dto.nickname()).ifPresent(v -> {
                throw new ClientException(ResponseCode.DUPLICATE_NICKNAME);
            });
        }

        if (Objects.nonNull(dto.imageId())) {
            Image image = imageService.findById(user.getId());

            if (Objects.nonNull(image.getUser()) && !image.getUser().equals(user)) throw new ClientException(ResponseCode.IMAGE_OWNER_ALREADY_EXIST);
            image.updateUser(user);
            user.updateProfileImage(image);
            imageRepository.save(image);
        }
        user.updateNickname(dto.nickname());
        user.updateEmailNotify(dto.emailNotify());
        user.updateSmsNotify(dto.smsNotify());
        user.updatePostLikeAlarm(dto.postLikeAlarm());
        user.updatePostReviewAlarm(dto.postReviewAlarm());
        user.updatePopularPostAlarm(dto.popularPostAlarm());
        return user;
    }
}
