package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
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

    /** ── 카테고리별 최신 게시물 (24h -> 7d -> 30d -> 전체) ── */
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(Long currentUserId, String category, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> p24  = findByCreatedAfter(category, pg, Duration.ofHours(24));
        if (p24.getNumberOfElements() == size) return mapPosts(p24, currentUserId);

        Page<Post> p7d  = findByCreatedAfter(category, pg, Duration.ofDays(7));
        if (p7d.getNumberOfElements() == size) return mapPosts(p7d, currentUserId);

        Page<Post> p30d = findByCreatedAfter(category, pg, Duration.ofDays(30));
        if (p30d.hasContent()) return mapPosts(p30d, currentUserId);

        // fallback: 전체기간 조회
        Page<Post> all = postRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("category"), category),
                pg
        );
        return mapPosts(all, currentUserId);
    }

    private Page<Post> findByCreatedAfter(String category, Pageable pg, Duration ago) {
        LocalDateTime since = LocalDateTime.now().minus(ago);
        return postRepository.findAll(
                (root, q, cb) -> cb.and(
                        cb.equal(root.get("category"), category),
                        cb.greaterThanOrEqualTo(root.get("createdAt"), since)
                ),
                pg
        );
    }

    /** ── 오늘의 인기 조회수 TopN (VIEW 집계) ── */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(Long currentUserId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());
        Page<PostResponse> today = aggregateByAction("VIEW", Duration.ofDays(1), pg, currentUserId);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week  = aggregateByAction("VIEW", Duration.ofDays(7), pg, currentUserId);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("VIEW", Duration.ofDays(30), pg, currentUserId);
        if (month.hasContent()) return month;

        // fallback: 전체 viewCount 기준 정렬
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("viewCount").descending())
        ).map(p -> toDto(p, currentUserId));
    }

    /** ── 주간 인기 조회수 TopN ── */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(Long currentUserId, int page, int size) {
        return aggregateByAction("VIEW", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()), currentUserId);
    }

    /** ── 오늘의 인기 북마크 TopN (BOOKMARK 집계) ── */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(Long currentUserId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());
        Page<PostResponse> today = aggregateByAction("BOOKMARK", Duration.ofDays(1), pg, currentUserId);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week  = aggregateByAction("BOOKMARK", Duration.ofDays(7), pg, currentUserId);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("BOOKMARK", Duration.ofDays(30), pg, currentUserId);
        if (month.hasContent()) return month;

        // fallback: 전체 bookmarkCount 기준 정렬
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending())
        ).map(p -> toDto(p, currentUserId));
    }

    /** ── 주간 인기 북마크 TopN ── */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(Long currentUserId, int page, int size) {
        return aggregateByAction("BOOKMARK", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()), currentUserId);
    }

    /** ── 추천 탭: 오늘 북마크 Top + 인기 태그 ── */
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(Long currentUserId, int page, int size) {
        Page<PostResponse> posts = getTodayTopBookmarked(currentUserId, page, size);
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }

    /** ── VIEW/BOOKMARK action 집계 후 PostResponse로 매핑 ── */
    private Page<PostResponse> aggregateByAction(String action, Duration ago, Pageable pg, Long currentUserId) {
        LocalDateTime start = LocalDate.now().atStartOfDay().minus(ago.minusDays(1));
        LocalDateTime end   = LocalDate.now().atStartOfDay().plusDays(1);

        Page<Long> ids = switch (action) {
            case "VIEW"     -> actionLogRepository.findTopViewedPostIds(start, end, pg);
            case "BOOKMARK" -> actionLogRepository.findTopBookmarkedPostIds(start, end, pg);
            default         -> Page.empty(pg);
        };

        return ids.map(id -> toDto(postRepository.findById(id).orElseThrow(), currentUserId));
    }

    /** ── Post → PostResponse 변환 헬퍼 ── */
    private PostResponse toDto(Post p, Long currentUserId) {
        // 대표 이미지 URL 결정
        String firstImageUrl = !p.getImageIds().isEmpty()
                ? imageService.findById(p.getImageIds().get(0)).getUrl()
                : ImageUtils.getDefaultImageUrl();

        // 좋아요 개수
        long likeCount = postLikeRepository.countByPostId(p.getId());

        // 현재 사용자가 좋아요했는지
        boolean hasLiked = currentUserId != null
                && postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();

        // 현재 사용자가 북마크했는지
        boolean hasBookmarked = currentUserId != null
                && postBookmarkService.isBookmarked(p.getId(), currentUserId);

        // 별점 정보 (Service에서 반환하는 BigDecimal 기반 DTO → 내부 DTO로 변환)
        com.team1.mixIt.post.dto.response.RatingResponse extRating = ratingService.getRatingResponse(p.getId());
        // BigDecimal → Double 변환 (null 체크 포함)
        Double avg = (extRating.getAverageRating() != null)
                ? extRating.getAverageRating().doubleValue()
                : 0.0;
        long cnt = extRating.getRatingCount();
        PostResponse.RatingResponse ratingResp = new PostResponse.RatingResponse(avg, cnt);

        // 작성자 프로필 이미지 URL
        String authorProfileUrl = (p.getUser().getProfileImageId() != null)
                ? imageService.findById(p.getUser().getProfileImageId()).getUrl()
                : null;

        return PostResponse.fromEntity(
                p,
                currentUserId,
                firstImageUrl,
                imageService,
                postBookmarkService,
                ratingResp,
                likeCount,
                hasLiked
        );
    }

    /** ── Page<Post> → Page<PostResponse> 매핑 헬퍼 ── */
    private Page<PostResponse> mapPosts(Page<Post> posts, Long currentUserId) {
        return posts.map(p -> toDto(p, currentUserId));
    }
}
