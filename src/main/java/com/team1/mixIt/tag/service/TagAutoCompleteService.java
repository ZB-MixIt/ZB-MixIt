package com.team1.mixIt.tag.service;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.tag.dto.response.AutoCompleteResponse;
import com.team1.mixIt.tag.entity.TagSearchLog;
import com.team1.mixIt.tag.repository.TagSearchLogRepository;
import com.team1.mixIt.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagAutoCompleteService {
    private final TagSearchLogRepository logRepo;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public List<AutoCompleteResponse> autocomplete(String prefix,
                                                   int limit,
                                                   User user) {
        logRepo.save(TagSearchLog.builder()
                .tag(prefix)
                .user(user)
                .build()
        );

        String sql = """
            SELECT s.tag
              FROM tag_stats s
              LEFT JOIN (
                SELECT tag, COUNT(*) AS search_count
                  FROM tag_search_log
                 WHERE searched_at >= DATE_SUB(NOW(), INTERVAL :logDays DAY)
                   AND tag LIKE CONCAT(:prefix, '%')
                 GROUP BY tag
              ) l ON s.tag = l.tag
             WHERE s.tag LIKE CONCAT(:prefix, '%')
             ORDER BY (s.use_count * :w1
                     + COALESCE(l.search_count, 0) * :w2) DESC
             LIMIT :limit
        """;

        @SuppressWarnings("unchecked")
        List<String> tags = em.createNativeQuery(sql)
                .setParameter("prefix", prefix)
                .setParameter("limit", limit)
                .setParameter("logDays", 1)      // 최근 1일 검색량 반영
                .setParameter("w1", 0.7)         // 사용 빈도 가중치
                .setParameter("w2", 0.3)         // 검색량 가중치
                .getResultList();

        return tags.stream()
                .map(AutoCompleteResponse::new)
                .collect(Collectors.toList());
    }
}
