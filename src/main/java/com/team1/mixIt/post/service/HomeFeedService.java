package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeFeedService {
    private final PostRepository postRepository;
    private final ActionLogRepository actionLogRepository;

    // 홈: 카테고리별 최신 n개
    @Transactional(readOnly = true)
    public List<PostResponse> getHomeByCategory(String category, int n) {
        return postRepository.findAll(
                        (root, query, cb) -> cb.equal(root.get("category"), category),
                        PageRequest.of(0, n, Sort.by("createdAt").descending())
                ).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    // 홈: 오늘의 인기 조회수 Top 페이징 = 지금 인기있는 조합
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopViewed(int page, int size) {
        LocalDate today = LocalDate.now();
        Page<Long> ids = actionLogRepository.findTopViewedPostIds(today, PageRequest.of(page, size));
        return ids.map(id -> PostResponse.fromEntity(
                postRepository.findById(id).orElseThrow()
        ));
    }

    // 홈: 오늘의 추천 북마크 Top 페이징 = 오늘의 추천 게시물
    @Transactional(readOnly = true)
    public Page<PostResponse> getTodayTopBookmarked(int page, int size) {
        LocalDate today = LocalDate.now();
        Page<Long> ids = actionLogRepository.findTopBookmarkedPostIds(today, PageRequest.of(page, size));
        return ids.map(id -> PostResponse.fromEntity(
                postRepository.findById(id).orElseThrow()
        ));
    }
}
