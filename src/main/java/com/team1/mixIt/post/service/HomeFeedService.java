package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.service.TagStatsService;
import com.team1.mixIt.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 카테고리별 최근 24시간 게시물, 없으면 전체 최신순 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(String category, int page, int size) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> recent = postRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("category"), category),
                        cb.greaterThanOrEqualTo(root.get("createdAt"), since)
                ),
                pg
        );

        Page<Post> result = recent.hasContent()
                ? recent
                : postRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("category"), category),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        return result.map(p -> toDto(p));
    }

    /** 당일 조회수 Top N, 없으면 전체 조회수 순 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(int page, int size) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        Page<Long> ids = actionLogRepository.findTopViewedPostIds(
                todayStart, tomorrowStart,
                PageRequest.of(page, size)
        );

        Page<PostResponse> viewed = ids.map(id -> toDto(postRepository.findById(id).orElseThrow()));

        if (viewed.hasContent()) return viewed;

        // fallback: 전체 게시물 조회수 순
        Page<Post> fallback = postRepository.findAll(
                PageRequest.of(page, size, Sort.by("viewCount").descending())
        );
        return fallback.map(p -> toDto(p));
    }

    /** 주간 조회수 Top N, 없으면 전체 조회수 순 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        Page<Object[]> raw = actionLogRepository.findWeeklyViews(
                weekAgo, now,
                PageRequest.of(page, size)
        );

        List<PostResponse> list = raw.getContent().stream()
                .map(arr -> toDto(postRepository.findById((Long) arr[0]).orElseThrow()))
                .toList();

        Page<PostResponse> weekly = new PageImpl<>(list, raw.getPageable(), raw.getTotalElements());
        if (weekly.hasContent()) return weekly;

        // fallback: 전체 조회수 순
        Page<Post> fallback = postRepository.findAll(
                PageRequest.of(page, size, Sort.by("viewCount").descending())
        );
        return fallback.map(p -> toDto(p));
    }

    /** 당일 북마크 Top N, 없으면 전체 북마크 순 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(int page, int size) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        Page<Long> ids = actionLogRepository.findTopBookmarkedPostIds(
                todayStart, tomorrowStart,
                PageRequest.of(page, size)
        );

        Page<PostResponse> bookmarked = ids.map(id -> toDto(postRepository.findById(id).orElseThrow()));

        if (bookmarked.hasContent()) return bookmarked;

        Page<Post> fallback = postRepository.findAll(
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending())
        );
        return fallback.map(p -> toDto(p));
    }

    /** 주간 북마크 Top N, 없으면 전체 북마크 순 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        Page<Long> ids = actionLogRepository.findWeeklyBookmarkedPostIds(
                weekAgo, now,
                PageRequest.of(page, size)
        );

        Page<PostResponse> weekly = ids.map(id -> toDto(postRepository.findById(id).orElseThrow()));
        if (weekly.hasContent()) return weekly;

        Page<Post> fallback = postRepository.findAll(
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending())
        );
        return fallback.map(p -> toDto(p));
    }

    /** 추천: 당일 북마크 Top + 인기 태그 */
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(int page, int size) {
        Page<PostResponse> posts = getTodayTopBookmarked(page, size);
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }

    // — 공통 DTO 변환: Post -> PostResponse
    private PostResponse toDto(Post p) {
        RatingResponse rating = ratingService.getRatingResponse(p.getId());
        return PostResponse.fromEntity(
                p,
                null,
                ImageUtils.getDefaultImageUrl(),
                imageService,
                postBookmarkService,
                rating
        );
    }
}
