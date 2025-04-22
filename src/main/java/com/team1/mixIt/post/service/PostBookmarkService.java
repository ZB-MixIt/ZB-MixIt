package com.team1.mixIt.post.service;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.BookmarkResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.repository.UserBookmarkRepository;
import com.team1.mixIt.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostBookmarkService {
    private final PostRepository postRepository;
    private final UserBookmarkRepository userBookmarkRepository;

    @Transactional
    public void addBookmark(Long postId, User user) {
        UserBookmarkId key = new UserBookmarkId(user.getId(), postId);

        if (!userBookmarkRepository.existsById(key)) {
            userBookmarkRepository.save(UserBookmark.builder()
                    .id(key)
                    .user(user)
                    .post(Post.builder().id(postId).build())
                    .build());
            postRepository.increaseBookmarkCount(postId);
        }
    }

    @Transactional
    public void removeBookmark(Long postId, User user) {
        UserBookmarkId key = new UserBookmarkId(user.getId(), postId);
        if (userBookmarkRepository.existsById(key)) {
            userBookmarkRepository.deleteById(key);
            postRepository.decreaseBookmarkCount(postId);
        }
    }


    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getMyBookmarks(Long userId, int page, int size) {
        return userBookmarkRepository
                .findAllByIdUserId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(ub -> {
                    Post post = ub.getPost();  // LAZY proxy 에서 로딩
                    return BookmarkResponse.fromEntity(post);
                });
    }
}