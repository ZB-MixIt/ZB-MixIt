package com.team1.mixIt.post.service;

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


@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final PostRepository postRepo;
    // private final ImageService imageService; // TODO: 이미지 업로드 처리

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

        // TODO: imageService를 통해 업로드된 이미지 저장 처리

        recalcAverageRating(post);
        return toResponse(review);
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

        // TODO: imageService를 통해 이미지 수정 처리

        recalcAverageRating(r.getPost());
        return toResponse(r);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review r = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "리뷰 " + reviewId + " 를 찾을 수 없습니다."
                ));

        Post post = r.getPost();
        reviewRepo.delete(r);

        // TODO: imageService를 통해 연관된 이미지 삭제 처리

        recalcAverageRating(post);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listReviews(Long postId) {
        return reviewRepo.findByPostIdOrderByRateDescCreatedAtDesc(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void recalcAverageRating(Post post) {
        BigDecimal avg = reviewRepo.findAverageRateByPostId(post.getId())
                .setScale(1, RoundingMode.HALF_UP);
        post.setAvgRating(avg);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userNickname(r.getUser().getNickname())  // DTO 필드이름과 일치
                .content(r.getContent())
                .rate(r.getRate())
                .createdAt(r.getCreatedAt())             // getCreatedAt() 사용
                .modifiedAt(r.getModifiedAt())           // getModifiedAt() 사용
                .imageIds(List.of())                     // TODO
                .build();
    }
}
