package com.study.ngy.domain.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorRawLogRepository extends JpaRepository<VisitorRawLog, Long> {

    /** 최근 방문 로그 N건 */
    List<VisitorRawLog> findTop100ByOrderByVisitedAtDesc();

    /** 기간 내 기기 유형별 카운트 */
    @Query("SELECT v.deviceType, COUNT(v) FROM VisitorRawLog v WHERE v.visitedAt >= :from GROUP BY v.deviceType")
    List<Object[]> countByDeviceTypeSince(@Param("from") LocalDateTime from);

    /** 기간 내 유입 소스별 카운트 */
    @Query("SELECT v.referrerSource, COUNT(v) FROM VisitorRawLog v WHERE v.visitedAt >= :from GROUP BY v.referrerSource ORDER BY COUNT(v) DESC")
    List<Object[]> countByReferrerSourceSince(@Param("from") LocalDateTime from);

    /** 세션별 방문 페이지 목록 — 행동 흐름 조회 */
    @Query("SELECT v FROM VisitorRawLog v WHERE v.sessionId = :sessionId ORDER BY v.visitedAt ASC")
    List<VisitorRawLog> findSessionFlow(@Param("sessionId") String sessionId);

    /** 기간 내 세션 목록 (최근 세션 순) */
    @Query("SELECT DISTINCT v.sessionId FROM VisitorRawLog v WHERE v.visitedAt >= :from AND v.sessionId IS NOT NULL ORDER BY v.sessionId DESC")
    List<String> findRecentSessionIds(@Param("from") LocalDateTime from);

    /** 기간 내 IP별 방문 횟수 상위 */
    @Query("SELECT v.ip, COUNT(v) FROM VisitorRawLog v WHERE v.visitedAt >= :from GROUP BY v.ip ORDER BY COUNT(v) DESC")
    List<Object[]> countByIpSince(@Param("from") LocalDateTime from);
}
