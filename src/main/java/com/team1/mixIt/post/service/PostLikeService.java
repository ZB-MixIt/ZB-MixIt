package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.entity.PostLike;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public LikeResponse addLike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("존재하지 않는 게시물입니다.");
        }

        boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, userId).isEmpty();
        if (hasLiked) {
            postLikeRepository.save(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build());
            postRepository.increaseLikeCount(postId);
        }
        long count = postLikeRepository.countByPostId(postId);
        return new LikeResponse(hasLiked, count);
    }

    @Transactional
    public void removeLike(Long postId, Long userId) {
        postLikeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    postLikeRepository.delete(like);
                    postRepository.decreaseLikeCount(postId);
                });
    }

    public LikeResponse status(Long postId, Long userId) {
        boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent();
        long count = postLikeRepository.countByPostId(postId);
        return new LikeResponse(hasLiked, count);
    }
}
