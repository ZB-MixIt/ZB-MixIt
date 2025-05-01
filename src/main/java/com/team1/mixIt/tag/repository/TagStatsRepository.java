package com.team1.mixIt.tag.repository;

import com.team1.mixIt.tag.entity.TagStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagStatsRepository extends JpaRepository<TagStats, String> {
    List<TagStats> findTopByTagStartingWithOrderByUseCountDesc(String prefix);
}
