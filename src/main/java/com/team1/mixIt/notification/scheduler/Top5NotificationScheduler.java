package com.team1.mixIt.notification.scheduler;

import com.team1.mixIt.notification.event.NotificationEvent;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.HomeFeedService;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
@RequiredArgsConstructor
public class Top5NotificationScheduler {

    private final HomeFeedService feedService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;  // ← 추가

    // 매일 자정
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyDailyTop5Views() {
        Page<PostResponse> top5 = feedService.getTodayTopViewed(null, 0, 5);
        top5.forEach(dto -> {
            User author = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new IllegalStateException("User not found: " + dto.getUserId()));

            // 인기게시물 알림이 켜져 있을 때만 발송
            if (!author.isPopularPostAlarm()) {
                return;
            }

            String msg = String.format("%s님의 조합이 오늘 인기 TOP5에 진입했습니다", dto.getAuthorNickname());
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    author.getId(),
                    "TOP5_VIEW",
                    dto.getId(),
                    msg
            ));
        });
    }

    // 매주 월요일 자정
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void notifyWeeklyTop5Bookmarks() {
        Page<PostResponse> top5 = feedService.getWeeklyTopBookmarked(null, 0, 5);
        top5.forEach(dto -> {
            User author = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new IllegalStateException("User not found: " + dto.getUserId()));

            if (!author.isPopularPostAlarm()) {
                return;
            }

            String msg = String.format("%s님의 조합이 지난 주 인기 북마크 TOP5에 진입했습니다", dto.getAuthorNickname());
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    author.getId(),
                    "TOP5_BOOKMARK",
                    dto.getId(),
                    msg
            ));
        });
    }
}
