package com.team1.mixIt.tag.service;

import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.entity.TagStats;
import com.team1.mixIt.tag.repository.TagStatsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TagStatsService {
    private final TagStatsRepository statsRepo;
    private final EntityManager em;

    @Transactional
    public void aggregateFromPostHashtag(int days) {
        statsRepo.deleteAllInBatch();
        List<Tuple> stats = em.createQuery("""
                SELECT ph.hashtag as tag, COUNT(ph) as cnt
                  FROM PostHashtag ph
                 WHERE ph.createdAt >= :since
                 GROUP BY ph.hashtag
                 ORDER BY cnt DESC
                """, Tuple.class)
                .setParameter("since", LocalDateTime.now().minusDays(days))
                .getResultList();

        List<TagStats> entities = stats.stream()
                .map(t -> TagStats.builder()
                        .tag(t.get("tag", String.class))
                        .useCount(t.get("cnt", Long.class))
                        .build())
                .toList();
        statsRepo.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<TagStatResponse> getTopTags(int limit) {
        var page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "useCount"));
        return statsRepo.findAll(page).getContent().stream()
                .map(ts -> new TagStatResponse(ts.getTag(), ts.getUseCount()))
                .toList();
    }
}
