package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.post.dto.response.BookmarkResponse;
import com.team1.mixIt.post.dto.response.BookmarkResponsePage;
import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.UserBookmarkRepository;
import com.team1.mixIt.user.entity.User;
import lombok.RequiredArgsConstructor;
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

        Page<BookmarkResponse> content = ubPage.map(ub ->
                BookmarkResponse.fromEntity(ub.getPost())
        );
        return BookmarkResponsePage.from(content);
    }

    // 현재 userId가 postId를 북마크했는지 여부를 반환
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, Long userId) {
        if (userId == null) return false;
        return userBookmarkRepository.existsByIdUserIdAndIdPostId(userId, postId);
    }
}
