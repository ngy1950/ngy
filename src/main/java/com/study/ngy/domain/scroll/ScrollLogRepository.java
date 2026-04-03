package com.study.ngy.domain.scroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScrollLogRepository extends JpaRepository<ScrollLog, Long> {

    /** 기간 내 페이지·깊이별 도달 횟수 */
    @Query("SELECT s.page, s.depth, COUNT(s) FROM ScrollLog s " +
           "WHERE s.recordedAt >= :from GROUP BY s.page, s.depth ORDER BY s.page, s.depth")
    List<Object[]> countByPageAndDepthSince(@Param("from") LocalDateTime from);
}
