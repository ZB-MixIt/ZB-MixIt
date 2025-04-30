package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;

import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
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

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

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
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));

        boolean hasLiked = postLikeRepository
                .findByPostIdAndUserId(postId, currentUserId)
                .isPresent();

        Long likeCount = postLikeRepository.countByPostId(postId);

        PostResponse reponse = mapToPostResponse(post);
        reponse.setHasLiked(hasLiked);
        reponse.setLikeCount(likeCount);
        return reponse;
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
                    var dto = mapToPostResponse(post);
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("내 글만 수정할 수 있습니다.");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageIds(nonNull(request.getImageIds()) ? request.getImageIds() : List.of());
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


    private PostResponse mapToPostResponse(Post post) {
        var response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setCategory(post.getCategory());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setBookmarkCount(post.getBookmarkCount());
        return response;
    }
}
