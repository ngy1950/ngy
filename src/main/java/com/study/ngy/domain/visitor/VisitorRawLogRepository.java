package com.study.ngy.domain.visitor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorRawLogRepository extends JpaRepository<VisitorRawLog, Long> {

    /** 최근 방문 로그 N건 (페이징 미적용 시 사용) */
    List<VisitorRawLog> findTop100ByOrderByVisitedAtDesc();

    /** 최근 방문 로그 페이징 처리 */
    Page<VisitorRawLog> findAllByOrderByVisitedAtDesc(Pageable pageable);

    /** IP별 가장 최근 로그만 가져오는 페이징 쿼리 */
    @Query(value = "SELECT v FROM VisitorRawLog v WHERE v.id IN (SELECT MAX(v2.id) FROM VisitorRawLog v2 GROUP BY v2.ip)",
           countQuery = "SELECT COUNT(DISTINCT v.ip) FROM VisitorRawLog v")
    Page<VisitorRawLog> findLatestLogsPerIp(Pageable pageable);

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

    /** 기간 내 순 방문자 수 (IP 기준 중복 제거) */
    @Query("SELECT COUNT(DISTINCT v.ip) FROM VisitorRawLog v WHERE v.visitedAt >= :from")
    Long countDistinctIpSince(@Param("from") LocalDateTime from);

    /** 일별 유입소스 집계 — 차트용 */
    @Query(value = "SELECT CAST(visited_at AS DATE), referrer_source, COUNT(*) " +
                   "FROM visitor_raw_log WHERE visited_at >= :from " +
                   "GROUP BY CAST(visited_at AS DATE), referrer_source " +
                   "ORDER BY CAST(visited_at AS DATE)", nativeQuery = true)
    List<Object[]> findDailyReferrerStats(@Param("from") LocalDateTime from);

    /** 체류 시간 업데이트용 — vid(sessionId) + page 기준 가장 최근 로그 1건 */
    @Query("SELECT v FROM VisitorRawLog v WHERE v.sessionId = :sessionId AND v.page = :page ORDER BY v.visitedAt DESC")
    List<VisitorRawLog> findTopBySessionIdAndPage(@Param("sessionId") String sessionId,
                                                  @Param("page") String page,
                                                  org.springframework.data.domain.Pageable pageable);

    /** 페이지별 평균 체류 시간 (측정된 것만) */
    @Query("SELECT v.page, AVG(v.dwellSeconds), COUNT(v) FROM VisitorRawLog v " +
           "WHERE v.visitedAt >= :from AND v.dwellSeconds IS NOT NULL " +
           "GROUP BY v.page ORDER BY COUNT(v) DESC")
    List<Object[]> avgDwellTimeByPageSince(@Param("from") LocalDateTime from);

    /** 이탈률 — 세션별 첫 진입 페이지 + 단일 페이지 여부 */
    @Query(value = "SELECT sub.first_page, " +
                   "SUM(CASE WHEN sub.page_cnt = 1 THEN 1 ELSE 0 END) AS bounced, " +
                   "COUNT(*) AS total " +
                   "FROM (SELECT session_id, MIN(page) AS first_page, COUNT(DISTINCT page) AS page_cnt " +
                   "      FROM visitor_raw_log " +
                   "      WHERE visited_at >= :from AND session_id IS NOT NULL " +
                   "      GROUP BY session_id) sub " +
                   "GROUP BY sub.first_page ORDER BY total DESC", nativeQuery = true)
    List<Object[]> getBounceStatsByPage(@Param("from") LocalDateTime from);

    /** 퍼널용 — 특정 페이지 방문 순 세션 수 */
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM VisitorRawLog v " +
           "WHERE v.page = :page AND v.visitedAt >= :from AND v.sessionId IS NOT NULL")
    Long countDistinctSessionsByPageSince(@Param("page") String page,
                                          @Param("from") LocalDateTime from);

    /** 퍼널용 — /menu/* 방문 순 세션 수 */
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM VisitorRawLog v " +
           "WHERE v.page LIKE '/menu/%' AND v.visitedAt >= :from AND v.sessionId IS NOT NULL")
    Long countDistinctSessionsByMenuPageSince(@Param("from") LocalDateTime from);
}
