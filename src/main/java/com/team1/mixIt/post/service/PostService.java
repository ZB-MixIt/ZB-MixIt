// src/main/java/com/team1/mixIt/post/service/PostService.java
package com.team1.mixIt.post.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageService imageService;

    @Transactional
    public void createPost(Long userId, PostCreateRequest request) {
        Post post = Post.builder()
                .userId(userId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .imageIds(nonNull(request.getImageIds()) ? request.getImageIds() : List.of())
                .build();
        postRepository.save(post);

        if (nonNull(request.getImageIds()) && !request.getImageIds().isEmpty()) {
            List<Image> images = imageService.findAllById(request.getImageIds());
            User userProxy = userRepository.getReferenceById(userId);
            imageService.setOwner(images, userProxy);
        }
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));

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
            String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        var posts = postRepository.findAll((root, query, cb) -> {
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
        }, sort);

        return posts.stream()
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
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest request) {
        imageService.unassignAllFromPost(postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("내 글만 수정할 수 있습니다.");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageIds(nonNull(request.getImageIds()) ? request.getImageIds() : List.of());

        if (nonNull(request.getImageIds()) && !request.getImageIds().isEmpty()) {
            List<Image> newImgs = imageService.findAllById(request.getImageIds());
            User userProxy = userRepository.getReferenceById(userId);
            imageService.setOwner(newImgs, userProxy);
        }
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("내 글만 삭제할 수 있습니다.");
        }
        postRepository.delete(post);
    }
}
