package com.team1.mixIt.post.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.repository.ImageRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.ReviewRequest;
import com.team1.mixIt.post.dto.response.ReviewResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.ReviewRepository;
import com.team1.mixIt.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final PostRepository postRepo;
    private final ImageRepository imageRepo;
    private final ImageService imageService;

    @Transactional
    public ReviewResponse addReview(Long postId, User user, ReviewRequest req) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시물 " + postId + " 를 찾을 수 없습니다."
                ));

        Review review = reviewRepo.save(
                Review.builder()
                        .user(user)
                        .post(post)
                        .content(req.getContent())
                        .rate(req.getRate())
                        .build()
        );

        if (!req.getImageIds().isEmpty()) {
            List<Image> images = imageRepo.findAllById(req.getImageIds());
            images.forEach(img -> img.updateReview(review));
            imageService.setOwner(images, user);
            imageRepo.saveAll(images);
            review.getImages().addAll(images);
        }

        recalcAverageRating(post);
        return ReviewResponse.fromEntity(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, User user, ReviewRequest req) {
        Review r = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "리뷰 " + reviewId + " 를 찾을 수 없습니다."
                ));

        r.setContent(req.getContent());
        r.setRate(req.getRate());

        //기존 리뷰 이미지 관계 해제
        List<Image> old = r.getImages();
        old.forEach(img -> img.updateReview(null));
        imageRepo.saveAll(old);
        r.getImages().clear();

        // 새로 업뎃된 이미지 연결
        if (!req.getImageIds().isEmpty()) {
            List<Image> images = imageRepo.findAllById(req.getImageIds());
            images.forEach(img -> img.updateReview(r));
            imageRepo.saveAll(images);
            r.getImages().addAll(images);
        }

        recalcAverageRating(r.getPost());
        return ReviewResponse.fromEntity(r);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review r = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "리뷰 " + reviewId + " 를 찾을 수 없습니다."
                ));

        Post post = r.getPost();

        // 이미지 삭제
        List<Image> images = r.getImages();
        images.forEach(imageService::delete);

        reviewRepo.delete(r);
        recalcAverageRating(post);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listReviews(Long postId) {
        return reviewRepo.findByPostIdOrderByRateDescCreatedAtDesc(postId).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void recalcAverageRating(Post post) {
        BigDecimal avg = reviewRepo.findAverageRateByPostId(post.getId())
                .setScale(1, RoundingMode.HALF_UP);
        double avgDouble = avg.doubleValue();
        post.setAvgRating(avgDouble);
    }
}
