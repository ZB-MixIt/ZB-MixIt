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

    public static final String DEFAULT_IMAGE_URL =
            "https://mixit-local.s3.ap-northeast-2.amazonaws.com/e94bb2e2-9symbol.png";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHashtagRepository hashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageService imageService;
    private final ActionLogRepository actionLogRepository;
    private final PostBookmarkService postBookmarkService;

    @Transactional
    public Long createPost(Long userId, PostCreateRequest req) {
        Category cat = req.getCategory();
        List<Long> imageIds = nonNull(req.getImageIds()) ? req.getImageIds() : List.of();

        Post post = Post.builder()
                .userId(userId)
                .category(cat)
                .title(req.getTitle())
                .content(req.getContent())
                .imageIds(imageIds)
                .build();
        post = postRepository.save(post);

        // 해시태그 저장
        for (String tag : req.getTags()) {
            PostHashtag ph = PostHashtag.builder()
                    .post(post)
                    .hashtag(tag)
                    .build();
            hashtagRepository.save(ph);
            post.getHashtag().add(ph);
        }

        // 이미지 소유권 할당
        if (!imageIds.isEmpty()) {
            List<Image> imgs = imageService.findAllById(imageIds);
            User u = userRepository.getReferenceById(userId);
            imageService.setOwner(imgs, u);
        }
        return post.getId();
    }

    @Transactional
    public PostResponse getPostById(Long postId, Long currentUserId, ImageService imageService) {
        postRepository.increaseViewCount(postId);
        actionLogRepository.save(ActionLog.builder()
                .postId(postId)
                .userId(currentUserId)
                .actionType("VIEW")
                .build()
        );

        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));

        boolean hasLiked = postLikeRepository
                .findByPostIdAndUserId(postId, currentUserId)
                .isPresent();
        long likeCnt = postLikeRepository.countByPostId(postId);

        PostResponse dto = PostResponse.fromEntity(p, currentUserId, DEFAULT_IMAGE_URL, imageService,postBookmarkService);
        dto.setHasLiked(hasLiked);
        dto.setLikeCount(likeCnt);
        return dto;
    }

    @Transactional(readOnly = true)
    public Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
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
        Pageable pg = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<Post> posts = postRepository.findAll((root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (category != null) {
                preds.add(cb.equal(root.get("category"), category));
            }
            if (keyword != null) {
                String like = "%" + keyword + "%";
                Predicate t1 = cb.like(root.get("title"), like);
                Join<Post, PostHashtag> jh = root.join("hashtag", JoinType.LEFT);
                Predicate t2 = cb.like(jh.get("hashtag"), like);
                preds.add(cb.or(t1, t2));
                query.distinct(true);
            }
            return cb.and(preds.toArray(new Predicate[0]));
        }, pg);

        return posts.stream()
                .map(p -> {
                    boolean liked = postLikeRepository
                            .findByPostIdAndUserId(p.getId(), currentUserId)
                            .isPresent();
                    long cnt = postLikeRepository.countByPostId(p.getId());
                    PostResponse dto = PostResponse.fromEntity(
                            p, currentUserId, DEFAULT_IMAGE_URL, imageService, postBookmarkService);

                    dto.setHasLiked(liked);
                    dto.setLikeCount(cnt);
                    return dto;
                })
                .toList();
    }


    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest req) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        if (!p.getUserId().equals(userId)) {
            throw new ClientException(ResponseCode.FORBIDDEN);
        }

        p.setCategory(req.getCategory());
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());

        List<Long> orig = p.getImageIds();
        List<Long> updated = nonNull(req.getImageIds()) ? req.getImageIds() : List.of();
        imageService.updateAssignedImages(orig, updated);
        p.setImageIds(updated);

        hashtagRepository.deleteByPost(p);
        p.getHashtag().clear();
        if (nonNull(req.getTags())) {
            for (String tag : req.getTags()) {
                PostHashtag ph = PostHashtag.builder()
                        .post(p)
                        .hashtag(tag)
                        .build();
                hashtagRepository.save(ph);
                p.getHashtag().add(ph);
            }
        }

        if (!updated.isEmpty()) {
            List<Image> imgs = imageService.findAllById(updated);
            User u = userRepository.getReferenceById(userId);
            imageService.setOwner(imgs, u);
        }
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        if (!p.getUserId().equals(userId)) {
            throw new ClientException(ResponseCode.FORBIDDEN);
        }
        postRepository.delete(p);
    }

    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }
}
