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
import com.team1.mixIt.post.entity.ReviewLike;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.ReviewLikeRepository;
import com.team1.mixIt.post.repository.ReviewRepository;
import com.team1.mixIt.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final PostRepository postRepo;
    private final ReviewLikeRepository reviewLikeRepo;
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
                .imageIds(newImageIds)
                .build();
        review = reviewRepo.save(review);

        if (!newImageIds.isEmpty()) {
            List<Image> images = imageService.findAllById(newImageIds);
            imageService.setOwner(images, user);
        }

        Long receiverId = post.getUserId();
        if (!receiverId.equals(user.getId()) && post.getUser().isPostReviewAlarm()) {
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

        List<Long> originalIds = review.getImageIds();
        List<Long> newImageIds = req.getImageIds() != null ? req.getImageIds() : List.of();
        imageService.updateAssignedImages(originalIds, newImageIds);

        review.setImageIds(newImageIds);

        List<Image> toOwn = imageService.findAllById(newImageIds);
        imageService.setOwner(toOwn, user);

        return ReviewResponse.fromEntity(review, user.getId(), imageService);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepo.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ClientException(ResponseCode.REVIEW_NOT_FOUND));
        Post post = review.getPost();

        reviewLikeRepo.deleteByReviewId(reviewId);


        reviewRepo.delete(review);
    }


    public boolean existsByIdAndPostId(Long reviewId, Long postId) {
        return reviewRepo.existsByIdAndPostId(reviewId, postId);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewResponse> listReviews(Long postId, Long currentUserId) {
        List<Review> reviews = reviewRepo.findByPostIdOrderByCreatedAtDesc(postId);

        Set<Long> likedIds = (currentUserId == null)
                ? Collections.emptySet()
                : reviewLikeRepo
                .findAllByUserIdAndReviewIdIn(currentUserId,
                        reviews.stream().map(Review::getId).toList()
                ).stream()
                .map(ReviewLike::getReviewId)
                .collect(Collectors.toSet());

        return reviews.stream().map(r -> {
            ReviewResponse dto = ReviewResponse.fromEntity(r, currentUserId, imageService);
            long realCount = reviewLikeRepo.countByReviewId(r.getId());
            dto.setLikeCount(realCount);

            dto.setHasLiked(likedIds.contains(r.getId()));            dto.setHasLiked(likedIds.contains(r.getId()));
            return dto;
        }).toList();
    }
}


