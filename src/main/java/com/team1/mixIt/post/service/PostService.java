package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;

import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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
    public void createPost(PostCreateRequest request) {
        // 테스트용 현재 로그인한 사용자 ID는 1로 하드코딩
        // TODO: 실제 로그인 유저 ID 로 교체
        Long currentUserId = 1L;

        Post post = Post.builder()
                .userId(currentUserId)
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
                .orElseThrow(() -> new RuntimeException("The post does not exist."));
        return mapToPostResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::mapToPostResponse).collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("The post does not exist."));
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageIds(nonNull(request.getImageIds()) ? request.getImageIds() : List.of());

    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("The post does not exist."));
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
