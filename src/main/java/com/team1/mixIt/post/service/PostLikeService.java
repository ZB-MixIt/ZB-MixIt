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
    public ResponseTemplate<LikeResponse> toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시물입니다."));

        var existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        boolean nowLiked;
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.setLikeCount(post.getLikeCount() - 1);
            nowLiked = false;
        } else {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            nowLiked = true;


        }
        postRepository.save(post);

        LikeResponse dto = new LikeResponse(nowLiked, post.getLikeCount());
        return ResponseTemplate.ok(dto);
    }

    @Transactional(readOnly = true)
    public ResponseTemplate<LikeResponse> status(Long postId, Long userId) {
        boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent();
        long likeCount = postLikeRepository.countByPostId(postId);
        return ResponseTemplate.ok(new LikeResponse(hasLiked, likeCount));
    }
}
