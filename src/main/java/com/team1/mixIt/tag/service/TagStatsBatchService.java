package com.team1.mixIt.tag.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagStatsBatchService {

    private final TagStatsService tagStatsService;

    // 스케줄러는 잠시 꺼두고
    // @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    // public void dailyAggregate() {
    //     tagStatsService.aggregateFromPostHashtag(1);
    // }

    @PostConstruct
    public void initAggregate() {
        // 앱 배포(시작)하자마자 태그 집계
        tagStatsService.aggregateFromPostHashtag(1);
        System.out.println(">>> 배포 직후 태그 집계 완료");
    }
}
