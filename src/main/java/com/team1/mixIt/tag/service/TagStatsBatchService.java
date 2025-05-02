package com.team1.mixIt.tag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagStatsBatchService {

    private final TagStatsService tagStatsService;

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyAggregate() {
        // 최근 1일 데이터 기준
        tagStatsService.aggregateFromPostHashtag(1);
    }
}
