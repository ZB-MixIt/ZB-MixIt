package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;

import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

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
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        return mapToPostResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::mapToPostResponse).collect(Collectors.toList());
    }


    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("내 글만 수정할 수 있습니다.");
        }
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setImageIds(nonNull(req.getImageIds()) ? req.getImageIds() : List.of());
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
        PostResponse response = new PostResponse();
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
