package com.team1.mixIt.actionlog.repository;

import com.team1.mixIt.actionlog.entity.ActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'VIEW'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    Page<Long> findTopViewedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );

    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'BOOKMARK'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    Page<Long> findTopBookmarkedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );

    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'LIKE'
         AND a.actionTime >= :todayStart
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC
    """)
    Page<Long> findTopLikedPostIds(
            @Param("todayStart") LocalDate todayStart,
            Pageable pageable
    );

    @Query("""
      SELECT a.postId, COUNT(a.id)
        FROM ActionLog a
       WHERE a.actionType = 'VIEW'
         AND a.actionTime BETWEEN :from AND :to
       GROUP BY a.postId
    """)
    List<Object[]> countViewsByPostBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}
