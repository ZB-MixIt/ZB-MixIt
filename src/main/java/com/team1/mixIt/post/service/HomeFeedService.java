package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.common.dto.InfinitePage;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.service.TagStatsService;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
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
    private final UserRepository userRepository;


    /**
     * 홈: 카테고리별 최신 게시물 (24h -> 7d -> 30d -> 전체)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getHomeByCategory(
            Long currentUserId,
            String category,
            int page,
            int size,
            String sort
    ) {
        Sort order = switch (sort) {
            case "popular" -> Sort.by("viewCount").descending();
            case "bookmark"-> Sort.by("bookmarkCount").descending();
            default        -> Sort.by("createdAt").descending();
        };

        Pageable pg = PageRequest.of(page, size, order);

        Page<Post> posts = postRepository.findAll(
                (root, query, cb) -> {
                    query.distinct(true);
                    return cb.equal(root.get("category"), category);
                },
                pg
        );

        return posts.map(p -> toDto(p, currentUserId));
    }

    private Duration pickWindow(String category, int size) {
        // 24시간
        Page<Post> p24 = findByCreatedAfter(category, 0, size, Duration.ofHours(24));
        if (p24.getNumberOfElements() == size) {
            return Duration.ofHours(24);
        }

        // 7일
        Page<Post> p7d = findByCreatedAfter(category, 0, size, Duration.ofDays(7));
        if (p7d.getNumberOfElements() == size) {
            return Duration.ofDays(7);
        }

        // 30일
        Page<Post> p30d = findByCreatedAfter(category, 0, size, Duration.ofDays(30));
        if (p30d.hasContent()) {
            return Duration.ofDays(30);
        }

        // 그 외에는 전체
        return null;
    }

    private Page<Post> findByCreatedAfter(
            String category, int page, int size, Duration ago
    ) {
        Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());
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
     * 홈: 오늘의 인기 조회수 TopN (1d -> 7d -> 30d -> 전체 viewCount)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(Long currentUserId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());

        Page<PostResponse> today = aggregateByAction("VIEW", Duration.ofDays(1), pg, currentUserId);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week = aggregateByAction("VIEW", Duration.ofDays(7), pg, currentUserId);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("VIEW", Duration.ofDays(30), pg, currentUserId);
        if (month.hasContent()) return month;

        // fallback: 전체 viewCount 컬럼 순
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("viewCount").descending())
        ).map(p -> toDto(p, currentUserId));
    }

    /**
     * 홈: 주간 인기 조회수 TopN (최근 7일 action_log 집계)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopViewed(Long currentUserId, int page, int size) {
        // 이번 주(7일) 집계만 하고, 부족해도 추가 fallback 없이 그대로 넘겨요.
        return aggregateByAction("VIEW", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()), currentUserId);
    }

    /**
     * 홈: 인기 조합 더보기 (동일 as 오늘의 인기 조회수, but pageable)
     */
    public Page<PostResponse> getPopularCombos(Long currentUserId, int page, int size) {
        return getTodayTopViewed(currentUserId, page, size);
    }

    /**
     * 홈: 오늘의 추천 북마크 TopN (1d -> 7d -> 30d -> 전체 bookmarkCount)
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(Long currentUserId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.unsorted());

        Page<PostResponse> today = aggregateByAction("BOOKMARK", Duration.ofDays(1), pg, currentUserId);
        if (today.getNumberOfElements() == size) return today;

        Page<PostResponse> week = aggregateByAction("BOOKMARK", Duration.ofDays(7), pg, currentUserId);
        if (week.getNumberOfElements() == size) return week;

        Page<PostResponse> month = aggregateByAction("BOOKMARK", Duration.ofDays(30), pg, currentUserId);
        if (month.hasContent()) return month;

        // fallback: 전체 bookmarkCount 순
        return postRepository.findAll(
                PageRequest.of(page, size, Sort.by("bookmarkCount").descending())
        ).map(p -> toDto(p, currentUserId));
    }

    /**
     * 홈: 주간 인기 북마크 TopN
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getWeeklyTopBookmarked(Long currentUserId, int page, int size) {
        return aggregateByAction("BOOKMARK", Duration.ofDays(7),
                PageRequest.of(page, size, Sort.unsorted()), currentUserId);
    }

    /**
     * 홈: 추천 탭 (오늘 북마크된 게시물 + 인기 태그 10개)
     */
    @Transactional(readOnly = true)
    public HomeFeedResponse getTodayRecommendations(Long currentUserId, int page, int size) {
        Page<PostResponse> posts = getTodayTopBookmarked(currentUserId, page, size);
        List<TagStatResponse> tags = tagStatsService.getTopTags(10);
        return new HomeFeedResponse(posts, tags);
    }
    /**
     * action 로그 집계 후 PostResponse로 매핑 (VIEW/BOOKMARK)
     */
    private Page<PostResponse> aggregateByAction(String action, Duration ago, Pageable pg, Long currentUserId) {
        LocalDateTime start = LocalDate.now().atStartOfDay().minus(ago.minusDays(1));
        LocalDateTime end = LocalDate.now().atStartOfDay().plusDays(1);

        Page<Long> ids = switch (action) {
            case "VIEW" ->
                    actionLogRepository.findTopViewedPostIds(start, end, pg);
            case "BOOKMARK" ->
                    actionLogRepository.findTopBookmarkedPostIds(start, end, pg);
            default -> Page.empty(pg);
        };

        return ids.map(id -> toDto(
                postRepository.findById(id).orElseThrow(), currentUserId
        ));
    }

    /**
     * Post -> PostResponse 변환 헬퍼
     */
    private PostResponse toDto(Post p, Long currentUserId) {
        // 이미지 리스트(ImageDto)
        List<PostResponse.ImageDto> imgDtos = p.getImageIds().stream()
                .map(imageService::findById)
                .map(img -> new PostResponse.ImageDto(img.getId(), img.getUrl()))
                .toList();

        // 대표 이미지 URL: 이미지가 없으면 기본 URL
        String defaultImageUrl;
        if (!p.getImageIds().isEmpty()) {
            Long firstImageId = p.getImageIds().get(0);
            defaultImageUrl = imageService.findById(firstImageId).getUrl();
        } else {
            defaultImageUrl = ImageUtils.getDefaultImageUrl();
        }

        // 좋아요 수와 현재 유저가 눌렀는지 여부
        long likeCount = postLikeRepository.countByPostId(p.getId());
        boolean hasLiked = (currentUserId != null) &&
                postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();

        // 별점 정보
        RatingResponse ratingResp = ratingService.getRatingResponse(p.getId());

        // 작성자 정보: User 엔티티에서 닉네임과 프로필 이미지 조회
        User author = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new IllegalStateException("작성자 정보 없음"));
        String authorNickname = author.getNickname();
        String authorProfileImage = null;
        if (author.getProfileImage() != null) {
            authorProfileImage = author.getProfileImage().getUrl();
        }

        // 북마크 여부
        boolean hasBookmarked = (currentUserId != null) &&
                postBookmarkService.isBookmarked(p.getId(), currentUserId);

        // 작성자 여부 판정
        boolean isAuthor = (currentUserId != null) && p.getUserId().equals(currentUserId);

        // 최종 빌드
        return PostResponse.fromEntity(
                p,
                currentUserId,
                defaultImageUrl,
                imageService,
                postBookmarkService,
                ratingResp,
                likeCount,
                hasLiked
        );
    }

    /**
     * Page<Post> -> Page<PostResponse> 매핑 헬퍼
     */
    private Page<PostResponse> mapPosts(Page<Post> posts, Long currentUserId) {
        return posts.map(p -> toDto(p, currentUserId));
    }


    private <T> InfinitePage<T> toInfinitePage(Page<T> pg) {
        InfinitePage<T> inf = new InfinitePage<>();
        inf.setPage(pg.getNumber());
        inf.setSize(pg.getSize());
        inf.setTotalPages(pg.getTotalPages());
        inf.setTotalElements(pg.getTotalElements());
        inf.setContent(pg.getContent());
        inf.setEmptyMessage(pg.hasContent() ? null : "게시물이 없습니다");
        inf.setNextPage(pg.hasNext() ? pg.getNumber() + 1 : null);
        return inf;
    }
}
