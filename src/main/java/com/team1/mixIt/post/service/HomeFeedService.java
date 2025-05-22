package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
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

    /** 홈: 카테고리별 최신 게시물 (24h -> 7d -> 30d -> 전체) */
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(String category, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> p24 = findByCreatedAfter(category, pg, Duration.ofHours(24));
        if (p24.getNumberOfElements() == size) return mapPosts(p24);

        Page<Post> p7d = findByCreatedAfter(category, pg, Duration.ofDays(7));
        if (p7d.getNumberOfElements() == size) return mapPosts(p7d);

        Page<Post> p30d = findByCreatedAfter(category, pg, Duration.ofDays(30));
        if (p30d.hasContent()) return mapPosts(p30d);

        // fallback: 전체기간 동일 Pageable
        Page<Post> all = postRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("category"), category),
                pg
        );
        return mapPosts(all);
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

    /** 홈: 오늘의 인기 조회수 TopN (1d -> 7d -> 30d -> 전체 viewCount) */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());

        Page<PostResponse> today = aggregateByAction("VIEW", Duration.ofDays(1), pg);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week = aggregateByAction("VIEW", Duration.ofDays(7), pg);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("VIEW", Duration.ofDays(30), pg);
        if (month.hasContent()) return month;

        // fallback: 전체 viewCount 컬럼 순
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("viewCount").descending())
        ).map(this::toDto);
    }

    /** 홈: 주간 인기 조회수 TopN (최근 7일 action_log 집계) */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(int page, int size) {
        // 이번 주(7일) 집계만 하고, 부족해도 추가 fallback 없이 그대로 넘겨요.
        return aggregateByAction("VIEW", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()));
    }

    /** 홈: 인기 조합 더보기 (동일 as 오늘의 인기 조회수, but pageable) */
    public Page<PostResponse> getPopularCombos(int page, int size) {
        return getTodayTopViewed(page, size);
    }

    /** 홈: 오늘의 추천 북마크 TopN (1d -> 7d -> 30d -> 전체 bookmarkCount) */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());

        Page<PostResponse> today = aggregateByAction("BOOKMARK", Duration.ofDays(1), pg);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week = aggregateByAction("BOOKMARK", Duration.ofDays(7), pg);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("BOOKMARK", Duration.ofDays(30), pg);
        if (month.hasContent()) return month;

        // fallback: 전체 bookmarkCount 순
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending())
        ).map(this::toDto);
    }

    /** 홈: 주간 인기 북마크 TopN */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(int page, int size) {
        return aggregateByAction("BOOKMARK", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()));
    }

    /** 홈: 추천 탭 (오늘 북마크된 게시물 + 인기 태그 10개) */
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(int page, int size) {
        Page<PostResponse> posts = getTodayTopBookmarked(page, size);
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }

    /** action 로그 집계 후 PostResponse로 매핑 (VIEW/BOOKMARK) */
    private Page<PostResponse> aggregateByAction(String action, Duration ago, Pageable pg) {
        LocalDateTime start = LocalDate.now().atStartOfDay().minus(ago.minusDays(1));
        LocalDateTime end   = LocalDate.now().atStartOfDay().plusDays(1);

        Page<Long> ids = switch (action) {
            case "VIEW"     -> actionLogRepository.findTopViewedPostIds(start, end, pg);
            case "BOOKMARK" -> actionLogRepository.findTopBookmarkedPostIds(start, end, pg);
            default         -> Page.empty(pg);
        };

        return ids.map(id -> {
            Post p = postRepository.findById(id).orElseThrow();
            return toDto(p);
        });
    }

    /** Post -> PostResponse 변환 헬퍼 */
    private PostResponse toDto(Post p) {
        return PostResponse.fromEntity(
                p,
                null,
                ImageUtils.getDefaultImageUrl(),
                imageService,
                postBookmarkService,
                ratingService.getRatingResponse(p.getId())
        );
    }

    /** Page<Post> -> Page<PostResponse> 매핑 헬퍼 */
    private Page<PostResponse> mapPosts(Page<Post> posts) {
        return posts.map(this::toDto);
    }
}
