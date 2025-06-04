package com.team1.mixIt.user.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.repository.ReviewRepository;
import com.team1.mixIt.user.controller.UserMyPageController;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMyPageService {

    private final ReviewRepository reviewRepository;
    private final ImageService imageService;


    public Page<UserMyPageController.MyReviewDto> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("user").get("id"), userId),
                pageable);

        return reviews.map(r -> {
            List<Image> images = imageService.findAllById(r.getImageIds());
            return UserMyPageController.MyReviewDto.of(r, images);
        });
    }

}
