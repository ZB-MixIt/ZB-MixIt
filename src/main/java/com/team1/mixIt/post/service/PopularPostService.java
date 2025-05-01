package com.team1.mixIt.post.service;

import com.team1.mixIt.actionlog.repository.ActionLogRepository;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularPostService {
    private final ActionLogRepository actionLogRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getTodayTopViewed(int limit) {
        LocalDate todayStart = LocalDate.now();
        List<Long> ids = actionLogRepository.findTopViewedPostIds(todayStart, PageRequest.of(0, limit));
        return postRepository.findAllById(ids).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getTodayTopBookmarked(int limit) {
        LocalDate todayStart = LocalDate.now();
        List<Long> ids = actionLogRepository.findTopBookmarkedPostIds(todayStart, PageRequest.of(0, limit));
        return postRepository.findAllById(ids).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getTodayTopLiked(int limit) {
        LocalDate todayStart = LocalDate.now();
        List<Long> ids = actionLogRepository.findTopLikedPostIds(todayStart, PageRequest.of(0, limit));
        return postRepository.findAllById(ids).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }
}
