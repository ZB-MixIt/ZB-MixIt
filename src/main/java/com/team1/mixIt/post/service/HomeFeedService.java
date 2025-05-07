package com.team1.mixIt.post.service;

import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.service.TagStatsService;
import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.team1.mixIt.post.service.PostService.DEFAULT_IMAGE_URL;

@Service
@RequiredArgsConstructor
public class HomeFeedService {

    private final PostRepository postRepository;
    private final ActionLogRepository actionLogRepository;
    private final TagStatsService tagStatsService;
    private final ImageService imageService;


    //  카테고리별 페이징 조회 (최근 24시간)
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(String category, int page, int size) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> posts = postRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("category"), category),
                        cb.greaterThanOrEqualTo(root.get("createdAt"), since)
                ),
                pg
        );

        return posts.map(p ->
                PostResponse.fromEntity(p, null, DEFAULT_IMAGE_URL, imageService)
        );
    }

    // 당일 조회수 Top N (페이징)
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(int page, int size) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        Page<Long> ids = actionLogRepository.findTopViewedPostIds(
                startOfToday,
                startOfTomorrow,
                PageRequest.of(page, size, Sort.unsorted())
        );

        return ids.map(id -> {
            Post p = postRepository.findById(id).orElseThrow();
            return PostResponse.fromEntity(p, null, DEFAULT_IMAGE_URL, imageService);
        });
    }


    // 주간 조회수 Top N (페이징)
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(int page, int size) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        Page<Object[]> raw = actionLogRepository.findWeeklyViews(
                weekAgo,
                now,
                PageRequest.of(page, size)
        );

        List<PostResponse> list = raw.getContent().stream()
                .map(arr -> {
                    Long id = (Long) arr[0];
                    Post p = postRepository.findById((Long)arr[0]).orElseThrow();
                    return PostResponse.fromEntity(p, null, DEFAULT_IMAGE_URL, imageService);
                })
                .toList();

        return new PageImpl<>(list, raw.getPageable(), raw.getTotalElements());
    }

    // 당일 북마크 Top N (페이징)
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(int page, int size) {
        // 오늘 00:00부터 내일 00:00 전까지의 범위 설정
        LocalDateTime startOfToday    = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        Page<Long> ids = actionLogRepository.findTopBookmarkedPostIds(
                startOfToday,
                startOfTomorrow,
                PageRequest.of(page, size, Sort.unsorted())
        );

        return ids.map(id -> {
            Post p = postRepository.findById(id).orElseThrow();
            return PostResponse.fromEntity(p, null, DEFAULT_IMAGE_URL, imageService);
        });
    }

   // 주간 북마크 TOP N
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(int page, int size) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        Page<Long> ids = actionLogRepository.findWeeklyBookmarkedPostIds(
                weekAgo, now, PageRequest.of(page, size, Sort.unsorted())
        );

        return ids.map(id -> {
            Post p = postRepository.findById(id).orElseThrow();
            return PostResponse.fromEntity(p, null, DEFAULT_IMAGE_URL, imageService);
        });
    }

    // 추천 탭: 당일 북마크된 게시물 + 인기 태그 10개
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(int page, int size) {
        Page<PostResponse> posts = getTodayTopBookmarked(page, size);
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }
}
