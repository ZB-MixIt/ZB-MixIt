package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.notification.event.NotificationEvent;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.entity.PostLike;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ActionLogRepository actionLogRepository;
    private final ApplicationEventPublisher eventPublisher;

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

            actionLogRepository.save(ActionLog.builder()
                    .postId(postId)
                    .userId(userId)
                    .actionType("LIKE")
                    .build());

            //알림 이벤트 발생
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    postRepository.getReferenceById(postId).getUserId(),
                    "POST_LIKE",
                    postId,
                    "회원님 게시물에 새 좋아요가 달렸습니다."
            ));
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

                    actionLogRepository.save(ActionLog.builder()
                            .postId(postId)
                            .userId(userId)
                            .actionType("UNLIKE")
                            .build());
                });
    }

    public LikeResponse status(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("존재하지 않는 게시물입니다.");
        }
        boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent();
        long count = postLikeRepository.countByPostId(postId);
        return new LikeResponse(hasLiked, count);
    }
}
