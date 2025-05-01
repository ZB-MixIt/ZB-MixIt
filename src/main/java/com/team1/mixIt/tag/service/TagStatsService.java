package com.team1.mixIt.tag.service;

import com.team1.mixIt.tag.entity.TagStats;
import com.team1.mixIt.tag.repository.TagStatsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagStatsService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final TagStatsRepository statsRepository;

    @Transactional
    public void aggregateFromPostHashtag(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 태그별 사용 횟수 집계
        List<Tuple> results = entityManager.createQuery(
                        "SELECT ph.hashtag AS tag, COUNT(ph) AS cnt " +
                                "  FROM PostHashtag ph " +
                                " WHERE ph.createdAt >= :since " +
                                " GROUP BY ph.hashtag", Tuple.class)
                .setParameter("since", since)
                .getResultList();

        Map<String, Long> counts = results.stream()
                .collect(Collectors.toMap(
                        t -> t.get("tag", String.class),
                        t -> t.get("cnt", Long.class)
                ));

        // tag_stats 테이블에 삽입 + 갱신
        counts.forEach((tag, cnt) -> {
            statsRepository.findById(tag).ifPresentOrElse(
                    existing -> {
                        existing.setUseCount(cnt);
                        existing.setUpdatedAt(LocalDateTime.now());
                        statsRepository.save(existing);
                    },
                    () -> {
                        TagStats ts = new TagStats();
                        ts.setTag(tag);
                        ts.setUseCount(cnt);
                        ts.setUpdatedAt(LocalDateTime.now());
                        statsRepository.save(ts);
                    }
            );
        });
    }
}
