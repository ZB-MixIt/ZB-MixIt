package com.team1.mixIt.actionlog.repository;

import com.team1.mixIt.actionlog.entity.ActionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    // 당일 VIEW Top 5
    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'VIEW'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    List<Long> findTopViewedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );

    // 당일 BOOKMARK Top 5
    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'BOOKMARK'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    List<Long> findTopBookmarkedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );

    // 당일 LIKE Top 5
    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'LIKE'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    List<Long> findTopLikedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );
}
