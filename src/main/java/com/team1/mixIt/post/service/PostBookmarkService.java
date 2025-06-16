package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.BookmarkResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRatingRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.UserBookmarkRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PostBookmarkService {
    private final PostRepository postRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final ActionLogRepository actionLogRepository;
    private final ImageService imageService;
    private final PostLikeRepository postLikeRepository;
    private final PostRatingRepository postRatingRepository;
    private final UserRepository userRepository;

    @Value("${mixit.default-image-url}")
    private String defaultImageUrl;

    @Transactional
    public void addBookmark(Long postId, User user) {
        if (!postRepository.existsById(postId)) {
            throw new ClientException(ResponseCode.POST_NOT_FOUND);
        }

        UserBookmarkId key = new UserBookmarkId(user.getId(), postId);
        if (!userBookmarkRepository.existsById(key)) {
            userBookmarkRepository.save(
                    UserBookmark.builder()
                            .id(key)
                            .user(user)
                            .post(postRepository.getReferenceById(postId))
                            .build()
            );
            postRepository.increaseBookmarkCount(postId);

            actionLogRepository.save(
                    ActionLog.builder()
                            .postId(postId)
                            .userId(user.getId())
                            .actionType("BOOKMARK")
                            .build()
            );
        }
    }

    @Transactional
    public void removeBookmark(Long postId, User user) {
        UserBookmarkId key = new UserBookmarkId(user.getId(), postId);
        if (userBookmarkRepository.existsById(key)) {
            userBookmarkRepository.deleteById(key);
            postRepository.decreaseBookmarkCount(postId);

            actionLogRepository.save(
                    ActionLog.builder()
                            .postId(postId)
                            .userId(user.getId())
                            .actionType("REMOVE_BOOKMARK")
                            .build()
            );
        }
    }
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getMyBookmarks(
            Long userId,
            int page,
            int size,
            Sort sort
    ) {
        Page<UserBookmark> ubPage = userBookmarkRepository.findAllByIdUserId(
                userId,
                PageRequest.of(page, size, sort)
        );

        Page<BookmarkResponse> responsePage = ubPage.map(ub ->
                BookmarkResponse.fromEntity(
                        ub.getPost(),
                        userId,
                        imageService,
                        postLikeRepository,
                        postRatingRepository,
                        userRepository,
                        defaultImageUrl
                )
        );

        return responsePage;
    }
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyBookmarksAsPostResponse(
            Long userId,
            int page,
            int size,
            Sort sort
    ) {
        return userBookmarkRepository.findAllByIdUserId(
                userId,
                PageRequest.of(page, size, sort)
        ).map(ub -> {
            Post p = ub.getPost();

            // 평균 평점 & 평점 개수
            BigDecimal avgBd = postRatingRepository.findAverageRateByPostId(p.getId());
            if (avgBd == null) {
                avgBd = BigDecimal.ZERO;
            }
            long cnt = postRatingRepository.countByPostId(p.getId());
            RatingResponse rating = new RatingResponse(avgBd, cnt);

            // 좋아요 수
            long likeCount = postLikeRepository.countByPostId(p.getId());
            boolean hasLiked = postLikeRepository
                    .findByPostIdAndUserId(p.getId(), userId)
                    .isPresent();

            return PostResponse.fromEntity(
                    p,
                    userId,
                    defaultImageUrl,
                    imageService,
                    this,
                    rating,
                    likeCount,
                    hasLiked
            );
        });
    }


    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, Long userId) {
        if (userId == null) return false;
        return userBookmarkRepository.existsByIdUserIdAndIdPostId(userId, postId);
    }
}


