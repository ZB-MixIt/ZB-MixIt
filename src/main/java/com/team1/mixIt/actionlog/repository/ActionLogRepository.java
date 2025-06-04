package com.team1.mixIt.actionlog.repository;

import com.team1.mixIt.actionlog.entity.ActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    // 오늘 조회수 로그를 집계해 조회수 순으로 포스트 ID 반환
    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'VIEW'
         AND a.actionTime BETWEEN :start AND :end
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC, MAX(a.actionTime) DESC
    """)
    Page<Long> findTopViewedPostIds(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            Pageable pageable
    );


    // 오늘 북마크 로그를 집계해 북마크 순으로 포스트 ID 반환
    @Query("""
      SELECT a.postId
        FROM ActionLog a
       WHERE a.actionType = 'BOOKMARK'
         AND a.actionTime BETWEEN :start AND :end
       GROUP BY a.postId
       ORDER BY COUNT(a.id) DESC, MAX(a.actionTime) DESC
    """)
    Page<Long> findTopBookmarkedPostIds(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            Pageable pageable
    );

    // 주간 북마크 TOP
    @Query("""
        SELECT al.postId
          FROM ActionLog al
         WHERE al.actionType = 'BOOKMARK'
           AND al.actionTime BETWEEN :start AND :end
         GROUP BY al.postId
         ORDER BY COUNT(al) DESC
    """)
    Page<Long> findWeeklyBookmarkedPostIds(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            Pageable pageable
    );

    // 지정 기간동안 VIEW 로그를 집계해 튜플 반환
    @Query("""
      SELECT a.postId AS postId, COUNT(a.id) AS cnt
        FROM ActionLog a
       WHERE a.actionType = 'VIEW'
         AND a.actionTime BETWEEN :from AND :to
       GROUP BY a.postId
       ORDER BY cnt DESC
    """)
    Page<Object[]> findWeeklyViews(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );
}