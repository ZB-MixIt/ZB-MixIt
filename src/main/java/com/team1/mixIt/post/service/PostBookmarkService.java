package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.BookmarkResponse;
import com.team1.mixIt.post.dto.response.BookmarkResponsePage;
import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRatingRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.UserBookmarkRepository;
import com.team1.mixIt.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostBookmarkService {
    private final PostRepository postRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final ActionLogRepository actionLogRepository;
    private final ImageService imageService;
    private final PostLikeRepository postLikeRepository;
    private final PostRatingRepository postRatingRepository;

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
    public BookmarkResponsePage getMyBookmarks(
            Long userId,
            int page,
            int size,
            Sort sort
    ) {
        Page<UserBookmark> ubPage = userBookmarkRepository.findAllByIdUserId(
                userId,
                PageRequest.of(page, size, sort)
        );

        Page<BookmarkResponse> content = ubPage.map(ub -> {
            return BookmarkResponse.fromEntity(
                    ub.getPost(),
                    userId,
                    imageService,
                    postLikeRepository,
                    postRatingRepository,
                    defaultImageUrl
            );
        });

        BookmarkResponsePage responsePage = BookmarkResponsePage.from(content);
        if (responsePage.getContent().isEmpty()) {
            responsePage.setEmptyMessage("더 많은 조합 보러가기");
        }
        return responsePage;
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, Long userId) {
        if (userId == null) return false;
        return userBookmarkRepository.existsByIdUserIdAndIdPostId(userId, postId);
    }
}
