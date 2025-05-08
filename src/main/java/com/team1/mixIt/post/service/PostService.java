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
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
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
    private final ReviewService reviewService;
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
    public PostResponse getPostById(
            Long postId,
            Long currentUserId,
            ImageService imageService,
            PostBookmarkService bookmarkService,
            PostRatingService ratingService
    ) {
        try {
            // 조회수 증가
            postRepository.increaseViewCount(postId);

            // 액션 로그 저장
            actionLogRepository.save(ActionLog.builder()
                    .postId(postId)
                    .userId(currentUserId)
                    .actionType("VIEW")
                    .build()
            );

            // 본문 게시물 조회
            Post p = postRepository.findById(postId)
                    .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));

            // 좋아요 여부 카운트
            boolean hasLiked = postLikeRepository
                    .findByPostIdAndUserId(postId, currentUserId)
                    .isPresent();
            long likeCnt = postLikeRepository.countByPostId(postId);

            // 평균 별점 참여자 수 조회
            RatingResponse rating = ratingService.getRatingResponse(p.getId());

            // 게시물 응답 DTO 생성
            PostResponse dto = PostResponse.fromEntity(
                    p,
                    currentUserId,
                    DEFAULT_IMAGE_URL,
                    imageService,
                    bookmarkService,
                    rating
            );

            // 좋아요 상태 및 수 설정
            dto.setHasLiked(hasLiked);
            dto.setLikeCount(likeCnt);

            return dto;
        } catch (ClientException e) {
            // 게시물이 없을 경우
            log.error("Post not found for ID: {}", postId, e);
            throw new ClientException(ResponseCode.POST_NOT_FOUND);  // 예외를 던져 클라이언트에 알림
        } catch (Exception e) {
            // 그 외 다른 예외 처리
            log.error("Error occurred while retrieving post with ID: {}", postId, e);
            throw new ServerException(ResponseCode.INTERNAL_SERVER_ERROR, e);  // 서버 오류 처리
        }
    }

    // 기본 조회
    @Transactional(readOnly = true)
    public Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));
    }

    // user + profileimage 동시 조회
    @Transactional(readOnly = true)
    public Post getPostWithUserAndProfile(Long postId) {
        return postRepository
                .findWithUserAndProfileImageById(postId)
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

                    RatingResponse rating = ratingService.getRatingResponse(p.getId());

                    PostResponse dto = PostResponse.fromEntity(
                            p,
                            currentUserId,
                            DEFAULT_IMAGE_URL,
                            imageService,
                            postBookmarkService,
                            rating
                    );

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

    @Transactional
    public void addOrUpdateRating(Long postId, Long userId, BigDecimal rate) {
        // 기존에 평가가 있는지 확인
        PostRating rating = postRatingRepository.findByPostIdAndUserId(postId, userId)
                .map(r -> {
                    r.setRate(rate);  // 기존 평점 수정
                    return r;
                })
                .orElse(PostRating.builder()
                        .postId(postId)
                        .userId(userId)
                        .rate(rate)
                        .build());  // 새로운 평점 추가

        // 평점 저장
        postRatingRepository.save(rating);

        // 게시물의 평균 평점 갱신
        updatePostAvgRating(postId);
    }

    // 게시물의 평균 평점을 계산하여 갱신하는 메서드
    private void updatePostAvgRating(Long postId) {
        // 평점 평균 계산
        BigDecimal avgRate = postRatingRepository.findAverageRateByPostId(postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ClientException(ResponseCode.POST_NOT_FOUND));

        post.setAvgRating(avgRate.doubleValue());  // 평균 평점 업데이트
        postRepository.save(post);  // 게시물 업데이트
    }
}
