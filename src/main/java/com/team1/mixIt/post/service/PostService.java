package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.entity.ActionLog;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.code.ResponseCode;
import com.team1.mixIt.common.exception.ClientException;
import com.team1.mixIt.common.exception.ServerException;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.PostHashtag;
import com.team1.mixIt.post.entity.PostRating;
import com.team1.mixIt.post.enums.Category;
import com.team1.mixIt.post.repository.PostHashtagRepository;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRatingRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import com.team1.mixIt.utils.ImageUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    @Value("${mixit.default-image-url}")
    private String defaultImageUrl;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHashtagRepository hashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageService imageService;
    private final ActionLogRepository actionLogRepository;
    private final PostBookmarkService postBookmarkService;
    private final PostRatingService ratingService;
    private final PostRatingRepository postRatingRepository;

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
                    .hashtag(tag.trim().toLowerCase())  // 소문자화하여 저장
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
    public PostResponse getPostById(Long postId,
                                    Long currentUserId,
                                    ImageService imageService,
                                    PostBookmarkService bookmarkService,
                                    PostRatingService ratingService,
                                    String defaultImageUrl) {
        try {
            // 조회수 증가
            postRepository.increaseViewCount(postId);

            // 액션 로그 저장
            actionLogRepository.save(ActionLog.builder()
                    .postId(postId)
                    .userId(currentUserId)
                    .actionType("VIEW")
                    .build());

            // 연관관계(작성자, 프로필 이미지, 해시태그) 전부 Fetch Join
            Post p = postRepository.findWithAllById(postId)
                    .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));

            // 좋아요 여부와 수
            boolean hasLiked = postLikeRepository.findByPostIdAndUserId(postId, currentUserId).isPresent();
            long likeCnt = postLikeRepository.countByPostId(postId);

            // 별점 정보
            RatingResponse rating = ratingService.getRatingResponse(postId);

            // DTO 변환
            PostResponse dto = PostResponse.fromEntity(
                    p, currentUserId, defaultImageUrl,
                    imageService, bookmarkService, rating,
                    likeCnt, hasLiked);
            dto.setHasLiked(hasLiked);
            dto.setLikeCount(likeCnt);

            return dto;
        } catch (ClientException e) {
            log.error("ID {} 게시물을 찾을 수 없습니다.", postId, e);
            throw new ClientException(ResponseCode.POST_NOT_FOUND);
        } catch (Exception e) {
            log.error("게시물 ID {} 조회 중 오류가 발생했습니다.", postId, e);
            throw new ServerException(ResponseCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Transactional(readOnly = true)
    public Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Post getPostWithUserAndProfile(Long postId) {
        return postRepository.findWithUserAndProfileImageById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Long currentUserId,
                                          Category category,
                                          String keyword,
                                          String sortBy,
                                          String sortDir,
                                          int page,
                                          int size) {
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

        return posts.stream().map(p -> {
            boolean liked = postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();
            long cnt = postLikeRepository.countByPostId(p.getId());
            RatingResponse ratingResp = ratingService.getRatingResponse(p.getId());

            PostResponse dto = PostResponse.fromEntity(
                    p, currentUserId, defaultImageUrl,
                    imageService, postBookmarkService, ratingResp,
                    cnt, liked);
            dto.setHasLiked(liked);
            dto.setLikeCount(cnt);
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("userId"), userId),
                pageable
        );
        return mapPosts(posts, userId);
    }

    private PostResponse toDto(Post p, Long currentUserId) {
        List<PostResponse.ImageDto> imgDtos = p.getImageIds().stream()
                .map(imageService::findById)
                .map(img -> new PostResponse.ImageDto(img.getId(), img.getUrl()))
                .toList();

        String defaultImgUrl;
        if (!p.getImageIds().isEmpty()) {
            Long firstImageId = p.getImageIds().get(0);
            defaultImgUrl = imageService.findById(firstImageId).getUrl();
        } else {
            defaultImgUrl = ImageUtils.getDefaultImageUrl();
        }

        long likeCount = postLikeRepository.countByPostId(p.getId());
        boolean hasLiked = (currentUserId != null) &&
                postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();

        RatingResponse ratingResp = ratingService.getRatingResponse(p.getId());

        User author = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new IllegalStateException("작성자 정보 없음"));
        String authorNickname = author.getNickname();
        String authorProfileImage = null;
        if (author.getProfileImage() != null) {
            authorProfileImage = author.getProfileImage().getUrl();
        }

        boolean hasBookmarked = (currentUserId != null) &&
                postBookmarkService.isBookmarked(p.getId(), currentUserId);
        boolean isAuthor = (currentUserId != null) && p.getUserId().equals(currentUserId);

        return PostResponse.fromEntity(
                p, currentUserId, defaultImgUrl,
                imageService, postBookmarkService,
                ratingResp, likeCount, hasLiked
        );
    }

    private Page<PostResponse> mapPosts(Page<Post> posts, Long currentUserId) {
        return posts.map(p -> toDto(p, currentUserId));
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new ClientException(ResponseCode.FORBIDDEN);
        }

        // 1) 기본 필드 업데이트
        post.setCategory(req.getCategory());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());

        // 2) 이미지 처리
        List<Long> orig = post.getImageIds();
        List<Long> updated = nonNull(req.getImageIds()) ? req.getImageIds() : List.of();
        imageService.updateAssignedImages(orig, updated);
        post.setImageIds(updated);

        // 3) 해시태그 동기화
        syncHashtags(post, req.getTags());

        // 4) 이미지 소유권 재할당
        assignImagesToUser(post.getImageIds(), userId);
    }

    @Transactional
    protected void syncHashtags(Post post, List<String> rawTags) {
        Set<String> cleaned = Optional.ofNullable(rawTags).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        //현재 엔티티가 가진 해시태그 문자열 세트
        Set<String> existing = post.getHashtag().stream()
                .map(PostHashtag::getHashtag)
                .collect(Collectors.toSet());

        // 삭제할 건: existing에는 있는데 cleaned에는 없는 것들
        List<PostHashtag> toDelete = post.getHashtag().stream()
                .filter(ph -> !cleaned.contains(ph.getHashtag()))
                .collect(Collectors.toList());
        for (PostHashtag ph : toDelete) {
            hashtagRepository.delete(ph);
            post.getHashtag().remove(ph);
        }

        // cleaned에는 있는데 existing에는 없는 것들
        for (String tag : cleaned) {
            if (!existing.contains(tag)) {
                PostHashtag ph = PostHashtag.builder()
                        .post(post)
                        .hashtag(tag)
                        .build();
                hashtagRepository.save(ph);
                post.getHashtag().add(ph);
            }
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

    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }

    @Transactional
    public void addOrUpdateRating(Long postId, Long userId, BigDecimal rate) {
        PostRating rating = postRatingRepository.findByPostIdAndUserId(postId, userId)
                .map(r -> {
                    r.setRate(rate);
                    return r;
                })
                .orElse(PostRating.builder()
                        .postId(postId)
                        .userId(userId)
                        .rate(rate)
                        .build());

        postRatingRepository.save(rating);
        updatePostAvgRating(postId);
    }

    private void updatePostAvgRating(Long postId) {
        BigDecimal avgRate = postRatingRepository.findAverageRateByPostId(postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
        post.setAvgRating(avgRate.doubleValue());
        postRepository.save(post);
    }

    private void assignImagesToUser(List<Long> imageIds, Long userId) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        List<Image> imgs = imageService.findAllById(imageIds);
        User u = userRepository.getReferenceById(userId);
        imageService.setOwner(imgs, u);
    }

}
