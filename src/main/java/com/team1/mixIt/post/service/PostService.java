package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.PostHashtag;
import com.team1.mixIt.post.repository.PostHashtagRepository;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHashtagRepository hashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageService imageService;
    private final ActionLogRepository actionLogRepository;

    @Transactional
    public Long createPost(Long userId, PostCreateRequest request) {

        List<Long> newImageIds = nonNull(request.getImageIds())
                ? request.getImageIds()
                : List.of();

        Post post = Post.builder()
                .userId(userId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .imageIds(newImageIds)
                .build();
        post = postRepository.save(post);

        // 태그 매핑 처리
        for (String tag : request.getTags()) {
            PostHashtag ph = PostHashtag.builder()
                    .post(post)
                    .hashtag(tag)
                    .build();
            hashtagRepository.save(ph);
            post.getHashtag().add(ph);
        }

        // 이미지 소유 처리
        if (!newImageIds.isEmpty()) {
            List<Image> images = imageService.findAllById(newImageIds);
            User userProxy = userRepository.getReferenceById(userId);
            imageService.setOwner(images, userProxy);
        }
        return post.getId();
    }

    @Transactional
    public PostResponse getPostById(Long postId, Long currentUserId) {
        postRepository.increaseViewCount(postId);

        // 조회 로그 기록
        actionLogRepository.save(ActionLog.builder()
                .postId(postId)
                .userId(currentUserId)
                .actionType("VIEW")
                .build());

        //  조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));

        boolean hasLiked = postLikeRepository
                .findByPostIdAndUserId(postId, currentUserId)
                .isPresent();

        long likeCount = postLikeRepository.countByPostId(postId);

        PostResponse dto = PostResponse.fromEntity(post);
        dto.setHasLiked(hasLiked);
        dto.setLikeCount(likeCount);
        return dto;
    }


    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(
            Long currentUserId,
            String category,
            String type,
            String keyword,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage = postRepository.findAll((root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (keyword != null) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("content"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return postPage.stream()
                .map(post -> {
                    PostResponse dto = PostResponse.fromEntity(post);
                    boolean hasLiked = postLikeRepository
                            .findByPostIdAndUserId(post.getId(), currentUserId)
                            .isPresent();
                    long likeCount = postLikeRepository.countByPostId(post.getId());
                    dto.setHasLiked(hasLiked);
                    dto.setLikeCount(likeCount);
                    return dto;
                })
                .toList();
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new ClientException(ResponseCode.FORBIDDEN);
        }

        List<Long> original = post.getImageIds();
        List<Long> newImageIds = nonNull(request.getImageIds()) ? request.getImageIds() : List.of();
        imageService.updateAssignedImages(original, newImageIds);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageIds(newImageIds);

        hashtagRepository.deleteByPost(post);
        post.getHashtag().clear();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                PostHashtag ph = PostHashtag.builder()
                        .post(post)
                        .hashtag(tag)
                        .build();
                hashtagRepository.save(ph);
                post.getHashtag().add(ph);
            }
        }

        if (!newImageIds.isEmpty()) {
            List<Image> newImgs = imageService.findAllById(newImageIds);
            User userProxy = userRepository.getReferenceById(userId);
            imageService.setOwner(newImgs, userProxy);
        }
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new ClientException(ResponseCode.FORBIDDEN);
        }
        postRepository.delete(post);
    }
}
