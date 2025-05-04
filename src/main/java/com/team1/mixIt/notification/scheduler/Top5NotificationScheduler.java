package com.team1.mixIt.notification.scheduler;

import com.team1.mixIt.notification.event.NotificationEvent;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.HomeFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class Top5NotificationScheduler {

    private final HomeFeedService feedService;
    private final ApplicationEventPublisher eventPublisher;

    // 매일 자정
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyDailyTop5Views() {
        Page<PostResponse> top5 = feedService.getTodayTopViewed(0,5);
        top5.forEach(dto ->
                eventPublisher.publishEvent(new NotificationEvent(
                        this,
                        dto.getUserId(),
                        "TOP5_VIEW",
                        dto.getId(),
                        "당신의 조합이 오늘 인기 TOP5에 진입했습니다"
                ))
        );
    }

    // 매주 월요일 자정
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void notifyWeeklyTop5Bookmarks() {
        Page<PostResponse> top5 = feedService.getWeeklyTopBookmarked(0,5);
        top5.forEach(dto ->
                eventPublisher.publishEvent(new NotificationEvent(
                        this,
                        dto.getUserId(),
                        "TOP5_BOOKMARK",
                        dto.getId(),
                        "당신의 조합이 지난 주 인기 북마크 TOP5에 진입했습니다"
                ))
        );
    }
}
