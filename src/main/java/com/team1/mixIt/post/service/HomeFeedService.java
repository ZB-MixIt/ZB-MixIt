package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.service.TagStatsService;
import com.team1.mixIt.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeFeedService {

    private final PostRepository postRepository;
    private final ActionLogRepository actionLogRepository;
    private final TagStatsService tagStatsService;
    private final ImageService imageService;
    private final PostBookmarkService postBookmarkService;
    private final PostRatingService ratingService;
    private final PostLikeRepository postLikeRepository;

    /**
     * 홈: 카테고리별 최신 게시물 (24h → 7d → 30d → 전체)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(
            Long currentUserId,
            String category,
            int page,
            int size
    ) {
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> p24 = findByCreatedAfter(category, pg, Duration.ofHours(24));
        if (p24.getNumberOfElements() == size) return mapPosts(p24, currentUserId);

        Page<Post> p7d = findByCreatedAfter(category, pg, Duration.ofDays(7));
        if (p7d.getNumberOfElements() == size) return mapPosts(p7d, currentUserId);

        Page<Post> p30d = findByCreatedAfter(category, pg, Duration.ofDays(30));
        if (p30d.hasContent()) return mapPosts(p30d, currentUserId);

        // fallback: 전체기간 동일 Pageable
        Page<Post> all = postRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("category"), category),
                pg
        );
        return mapPosts(all, currentUserId);
    }

    private Page<Post> findByCreatedAfter(
            String category,
            Pageable pg,
            Duration ago
    ) {
        LocalDateTime since = LocalDateTime.now().minus(ago);
        return postRepository.findAll(
                (root, q, cb) -> cb.and(
                        cb.equal(root.get("category"), category),
                        cb.greaterThanOrEqualTo(root.get("createdAt"), since)
                ),
                pg
        );
    }

    /**
     * 홈: 오늘의 인기 조회수 TopN
     *   • sortBy = "latest"  → createdAt 기준 정렬
     *   • sortBy = "popular" → 좋아요(likeCount) 기준 정렬
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(
            Long currentUserId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort;
        if ("popular".equalsIgnoreCase(sortBy)) {
            // 인기순(좋아요 수)
            sort = Sort.by(Sort.Direction.fromString(sortDir), "likeCount");
        } else {
            // 최신순(createdAt)
            sort = Sort.by(Sort.Direction.fromString(sortDir), "createdAt");
        }

        Pageable pg = PageRequest.of(page, size, sort);

        // 1) 오늘(1일) 기준 조회수 집계
        Page<PostResponse> today = aggregateByAction(
                "VIEW",
                Duration.ofDays(1),
                pg,
                currentUserId
        );
        if (today.getNumberOfElements() == size) return today;

        // 2) 7일 집계
        Page<PostResponse> week = aggregateByAction(
                "VIEW",
                Duration.ofDays(7),
                pg,
                currentUserId
        );
        if (week.getNumberOfElements() == size) return week;

        // 3) 30일 집계
        Page<PostResponse> month = aggregateByAction(
                "VIEW",
                Duration.ofDays(30),
                pg,
                currentUserId
        );
        if (month.hasContent()) return month;

        // 4) fallback: 전체 viewCount 기준 (정렬 기준=sort)
        return postRepository.findAll(PageRequest.of(page, size, sort))
                .map(p -> toDto(p, currentUserId));
    }

    /**
     * 홈: 주간 인기 조회수 TopN (최근 7일만, 순서 고정)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(
            Long currentUserId,
            int page,
            int size
    ) {
        return aggregateByAction(
                "VIEW",
                Duration.ofDays(7),
                PageRequest.of(page, size, Sort.by("viewCount").descending()),
                currentUserId
        );
    }

    /**
     * 홈: 인기 조합 더보기 (사실상 getTodayTopViewed와 동일 서명)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPopularCombos(
            Long currentUserId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        return getTodayTopViewed(currentUserId, page, size, sortBy, sortDir);
    }

    /**
     * 홈: 오늘의 추천 북마크 TopN
     *   • sortBy = "latest"  → createdAt 기준 정렬
     *   • sortBy = "popular" → 좋아요(likeCount) 기준 정렬
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(
            Long currentUserId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort;
        if ("popular".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.fromString(sortDir), "likeCount");
        } else {
            sort = Sort.by(Sort.Direction.fromString(sortDir), "createdAt");
        }

        Pageable pg = PageRequest.of(page, size, sort);

        // 1) 오늘(1일) 북마크 집계
        Page<PostResponse> today = aggregateByAction(
                "BOOKMARK",
                Duration.ofDays(1),
                pg,
                currentUserId
        );
        if (today.getNumberOfElements() == size) return today;

        // 2) 7일 집계
        Page<PostResponse> week = aggregateByAction(
                "BOOKMARK",
                Duration.ofDays(7),
                pg,
                currentUserId
        );
        if (week.getNumberOfElements() == size) return week;

        // 3) 30일 집계
        Page<PostResponse> month = aggregateByAction(
                "BOOKMARK",
                Duration.ofDays(30),
                pg,
                currentUserId
        );
        if (month.hasContent()) return month;

        // 4) fallback: 전체 bookmarkCount 기준 (정렬 기준=sort)
        return postRepository.findAll(PageRequest.of(page, size, sort))
                .map(p -> toDto(p, currentUserId));
    }

    /**
     * 홈: 주간 인기 북마크 TopN (최근 7일만)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(
            Long currentUserId,
            int page,
            int size
    ) {
        return aggregateByAction(
                "BOOKMARK",
                Duration.ofDays(7),
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending()),
                currentUserId
        );
    }

    /**
     * 홈: 추천 탭 (오늘 북마크된 게시물 + 인기 태그 Top10)
     *   • sortBy = "latest"  → createdAt
     *   • sortBy = "popular" → likeCount
     */
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(
            Long currentUserId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Page<PostResponse> posts = getTodayTopBookmarked(
                currentUserId, page, size, sortBy, sortDir
        );
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }

    /**
     * action 로그(VIEW/BOOKMARK) 집계 후 Page<PostResponse>로 매핑
     */
    private Page<PostResponse> aggregateByAction(
            String action,
            Duration ago,
            Pageable pg,
            Long currentUserId
    ) {
        LocalDateTime start = LocalDate.now().atStartOfDay().minus(ago.minusDays(1));
        LocalDateTime end   = LocalDate.now().atStartOfDay().plusDays(1);

        Page<Long> ids = switch (action) {
            case "VIEW"     -> actionLogRepository.findTopViewedPostIds(start, end, pg);
            case "BOOKMARK" -> actionLogRepository.findTopBookmarkedPostIds(start, end, pg);
            default         -> Page.empty(pg);
        };

        return ids.map(id -> toDto(postRepository.findById(id).orElseThrow(), currentUserId));
    }

    /**
     * Post → PostResponse 변환 헬퍼
     *   • authorNickname
     *   • authorProfileImage
     *   • rating (avg, count)
     *   • hasLiked, likeCount
     */
    private PostResponse toDto(Post p, Long currentUserId) {
        // 1) 대표 이미지 URL
        String firstImageUrl = !p.getImageIds().isEmpty()
                ? imageService.findById(p.getImageIds().get(0)).getUrl()
                : ImageUtils.getDefaultImageUrl();

        // 2) 좋아요 개수
        long likeCount = postLikeRepository.countByPostId(p.getId());

        // 3) 현재 사용자가 좋아요했는지
        boolean hasLiked = false;
        if (currentUserId != null) {
            hasLiked = postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();
        }

        // 4) 현재 사용자가 북마크했는지
        boolean hasBookmarked = false;
        if (currentUserId != null) {
            hasBookmarked = postBookmarkService.isBookmarked(p.getId(), currentUserId);
        }

        // 5) 별점 정보(BigDecimal → Double)
        RatingResponse extRating = ratingService.getRatingResponse(p.getId());


        // 6) 작성자 프로필 이미지 URL
        String authorProfileUrl = null;
        if (p.getUser().getProfileImageId() != null) {
            authorProfileUrl = imageService
                    .findById(p.getUser().getProfileImageId()).getUrl();
        }

        return PostResponse.fromEntity(
                p,
                currentUserId,
                firstImageUrl,
                imageService,
                postBookmarkService,
                extRating,
                likeCount,
                hasLiked
        );
    }

    /** Page<Post> → Page<PostResponse> 매핑 헬퍼 */
    private Page<PostResponse> mapPosts(Page<Post> posts, Long currentUserId) {
        return posts.map(p -> toDto(p, currentUserId));
    }
}
