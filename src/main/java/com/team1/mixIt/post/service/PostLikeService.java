package com.team1.mixIt.post.service;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.entity.Post;
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
    public ResponseTemplate<LikeResponse> addLike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("존재하지 않는 게시물입니다.");
        }

        // 2) 이미 좋아요를 안 눌렀을 때만
        if (postLikeRepository.findByPostIdAndUserId(postId, userId).isEmpty()) {
            postLikeRepository.save(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build());
            // 3) DB에서 바로 증가
            postRepository.increaseLikeCount(postId);
        }

        long count = postLikeRepository.countByPostId(postId);
        return ResponseTemplate.ok(new LikeResponse(true, count));
    }

    @Transactional
    public ResponseTemplate<Void> removeLike(Long postId, Long userId) {
        var existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 게시물입니다."));
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
        }
        return ResponseTemplate.ok();
    }

    public ResponseTemplate<LikeResponse> status(Long postId, Long userId) {
        boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent();
        long likeCount = postLikeRepository.countByPostId(postId);
        return ResponseTemplate.ok(new LikeResponse(hasLiked, likeCount));
    }
}
