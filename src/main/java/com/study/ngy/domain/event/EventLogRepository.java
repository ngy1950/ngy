package com.study.ngy.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    /** 기간 내 action·label별 클릭 수 */
    @Query("SELECT e.action, e.label, COUNT(e) FROM EventLog e " +
           "WHERE e.recordedAt >= :from GROUP BY e.action, e.label ORDER BY COUNT(e) DESC")
    List<Object[]> countByActionAndLabelSince(@Param("from") LocalDateTime from);

    /** 퍼널용 — 특정 action을 한 순 세션 수 */
    @Query("SELECT COUNT(DISTINCT e.sessionId) FROM EventLog e " +
           "WHERE e.action = :action AND e.recordedAt >= :from AND e.sessionId IS NOT NULL")
    Long countDistinctSessionsByActionSince(@Param("action") String action,
                                            @Param("from") LocalDateTime from);
}
