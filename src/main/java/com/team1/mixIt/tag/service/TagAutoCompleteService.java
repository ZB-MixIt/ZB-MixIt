package com.team1.mixIt.tag.service;

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
        // 검색어를 로그에 남기기
        logRepo.save(TagSearchLog.builder()
                .tag(prefix)
                .user(user)
                .build()
        );

        // 2) 두 개의 SELECT를 각각 괄호로 감싸고 UNION
        String sql = """
            (
              -- tag_stats에 있는 태그 우선 뽑기
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
               ORDER BY (s.use_count * :w1 + COALESCE(l.search_count, 0) * :w2) DESC
               LIMIT :limit
            )
            UNION
            (
              -- tag_stats에는 없지만, 최근 검색 로그에만 있는 태그
              SELECT DISTINCT tag
                FROM tag_search_log
               WHERE searched_at >= DATE_SUB(NOW(), INTERVAL :logDays DAY)
                 AND tag LIKE CONCAT(:prefix, '%')
                 AND tag NOT IN (
                     SELECT tag
                       FROM tag_stats
                      WHERE tag LIKE CONCAT(:prefix, '%')
                 )
               ORDER BY searched_at DESC
               LIMIT :limit
            )
            """;

        @SuppressWarnings("unchecked")
        List<String> tags = em.createNativeQuery(sql)
                .setParameter("prefix", prefix)
                .setParameter("limit", limit)
                .setParameter("logDays", 3)
                .setParameter("w1", 0.7)      // use_count 가중치
                .setParameter("w2", 0.3)      // search_count 가중치
                .getResultList();

        return tags.stream()
                .map(AutoCompleteResponse::new)
                .collect(Collectors.toList());
    }
}
