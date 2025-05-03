package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.PostHashtag;
import com.team1.mixIt.post.enums.Category;
import com.team1.mixIt.post.repository.PostHashtagRepository;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
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
        Category cat = request.getCategory();

        List<Long> newImageIds = nonNull(request.getImageIds())
                ? request.getImageIds()
                : List.of();

        Post post = Post.builder()
                .userId(userId)
                .category(cat)
                .title(request.getTitle())
                .content(request.getContent())
                .imageIds(newImageIds)
                .build();
        post = postRepository.save(post);

        // 태그 매핑
        for (String tag : request.getTags()) {
            PostHashtag ph = PostHashtag.builder()
                    .post(post)
                    .hashtag(tag)
                    .build();
            hashtagRepository.save(ph);
            post.getHashtag().add(ph);
        }

        // 이미지 소유권 할당
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
        actionLogRepository.save(ActionLog.builder()
                .postId(postId)
                .userId(currentUserId)
                .actionType("VIEW")
                .build()
        );

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
            Category category,
            String keyword,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage = postRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 카테고리 필터
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            // 키워드 검색 -> 수정 제목 또는 태그
            if (keyword != null) {
                String like = "%" + keyword + "%";
                Predicate titleMatch = cb.like(root.get("title"), like);

                Join<Post, PostHashtag> tagJoin = root.join("hashtag", JoinType.LEFT);
                Predicate tagMatch = cb.like(tagJoin.get("hashtag"), like);

                predicates.add(cb.or(titleMatch, tagMatch));
                query.distinct(true);
            }

            Predicate[] arr = predicates.toArray(new Predicate[0]);
            return cb.and(arr);
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
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("내 글만 수정할 수 있습니다.");
        }

        Category cat = request.getCategory();
        post.setCategory(cat);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        List<Long> original = post.getImageIds();
        List<Long> newImageIds = nonNull(request.getImageIds()) ? request.getImageIds() : List.of();
        imageService.updateAssignedImages(original, newImageIds);
        post.setImageIds(newImageIds);

        // 태그 재설정
        hashtagRepository.deleteByPost(post);
        post.getHashtag().clear();
        if (nonNull(request.getTags())) {
            for (String tag : request.getTags()) {
                PostHashtag ph = PostHashtag.builder()
                        .post(post)
                        .hashtag(tag)
                        .build();
                hashtagRepository.save(ph);
                post.getHashtag().add(ph);
            }
        }

        // 새 이미지 소유권
        if (!newImageIds.isEmpty()) {
            List<Image> newImgs = imageService.findAllById(newImageIds);
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
