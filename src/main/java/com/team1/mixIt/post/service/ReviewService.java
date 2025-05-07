package com.team1.mixIt.post.service;

import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.notification.event.NotificationEvent;
import com.team1.mixIt.post.dto.request.ReviewRequest;
import com.team1.mixIt.post.dto.response.ReviewResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.ReviewRepository;
import com.team1.mixIt.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final PostRepository postRepo;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReviewResponse addReview(Long postId, User user, ReviewRequest req) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        List<Long> newImageIds = req.getImageIds() != null ? req.getImageIds() : List.of();
        Review review = Review.builder()
                .user(user)
                .post(post)
                .content(req.getContent())
                .rate(req.getRate())
                .imageIds(newImageIds)
                .build();
        review = reviewRepo.save(review);

        if (!newImageIds.isEmpty()) {
            List<Image> images = imageService.findAllById(newImageIds);
            imageService.setOwner(images, user);
        }
        recalcAverageRating(post);

        Long receiverId = post.getUserId();
        if (!receiverId.equals(user.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    receiverId,
                    "POST_COMMENT",
                    postId,
                    String.format("%s님이 내 게시물에 댓글을 남겼습니다.", user.getNickname())
            ));
        }
        return ReviewResponse.fromEntity(review, user.getId(), imageService);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, User user, ReviewRequest req) {
        Review review = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ClientException(ResponseCode.REVIEW_NOT_FOUND));

        review.setContent(req.getContent());
        review.setRate(req.getRate());

        List<Long> originalIds = review.getImageIds();
        List<Long> newImageIds = req.getImageIds() != null ? req.getImageIds() : List.of();
        imageService.updateAssignedImages(originalIds, newImageIds);

        review.setImageIds(newImageIds);

        List<Image> toOwn = imageService.findAllById(newImageIds);
        imageService.setOwner(toOwn, user);

        recalcAverageRating(review.getPost());
        return ReviewResponse.fromEntity(review, user.getId(), imageService);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ClientException(ResponseCode.REVIEW_NOT_FOUND));
        Post post = review.getPost();
        reviewRepo.delete(review);
        recalcAverageRating(post);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewResponse> listReviews(Long postId, Long currentUserId) {
        return reviewRepo.findByPostIdOrderByRateDescCreatedAtDesc(postId)
                .stream()
                .map(r -> ReviewResponse.fromEntity(r, currentUserId, imageService))
                .toList();
    }

    @Transactional
    public void recalcAverageRating(Post post) {
        BigDecimal avg = reviewRepo.findAverageRateByPostId(post.getId())
                .setScale(1, RoundingMode.HALF_UP);
        post.setAvgRating(avg.doubleValue());
    }

    public boolean existsByIdAndPostId(Long reviewId, Long postId) {
        return reviewRepo.existsByIdAndPostId(reviewId, postId);
    }
}
